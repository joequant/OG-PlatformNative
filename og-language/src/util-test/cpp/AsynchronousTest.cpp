/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include <util/cpp/Asynchronous.h>

LOGGING (com.opengamma.language.util.AsynchronousTest);

#define TIMEOUT_COMPLETE	1000

class CTestAsyncOperation1 : public CAsynchronous::COperation {
private:
	int m_nRescheduleCount;
	int volatile *m_pnRunCount;
public:
	CTestAsyncOperation1 (int nRescheduleCount, int volatile *pnRunCount) {
		m_nRescheduleCount = nRescheduleCount;
		m_pnRunCount = pnRunCount;
	}
	void Run () {
		(*m_pnRunCount)++;
		if (m_nRescheduleCount > 0) {
			m_nRescheduleCount--;
			MustReschedule ();
		}
	}
	bool OnScheduled () {
		return m_nRescheduleCount >= 0;
	}
};

class CRCSemaphore {
private:
	CSemaphore m_sem;
	CAtomicInt m_oRC;
	~CRCSemaphore () {
		ASSERT (m_oRC.Get () == 0);
	}
public:
	CRCSemaphore () : m_oRC (1) { }
	void Retain () { m_oRC.IncrementAndGet (); }
	static void Release (CRCSemaphore *po) { if (!po->m_oRC.DecrementAndGet ()) delete po; }
	bool Wait () { return m_sem.Wait (); }
	bool Wait (unsigned long timeout) { return m_sem.Wait (timeout); }
	void Signal () { m_sem.Signal (); }
};

class CAsyncThreadControlOperation : public CAsynchronous::COperation {
private:
	CRCSemaphore *m_poWait;
	CRCSemaphore *m_poSignal;
public:
	CAsyncThreadControlOperation (CRCSemaphore *poWait, CRCSemaphore *poSignal) {
		poWait->Retain ();
		m_poWait = poWait;
		poSignal->Retain ();
		m_poSignal = poSignal;
	}
	~CAsyncThreadControlOperation () {
		CRCSemaphore::Release (m_poWait);
		CRCSemaphore::Release (m_poSignal);
	}
	void Run () {
		m_poSignal->Signal ();
		m_poWait->Wait ();
	}
};

class CAsyncThreadControl {
private:
	CRCSemaphore *m_poSemA;
	CRCSemaphore *m_poSemB;
public:
	CAsyncThreadControl () {
		m_poSemA = new CRCSemaphore ();
		m_poSemB = new CRCSemaphore ();
	}
	~CAsyncThreadControl () {
		CRCSemaphore::Release (m_poSemA);
		CRCSemaphore::Release (m_poSemB);
	}
	void Park () {
		// Short enough to cause tests to terminate eventually if things go wrong, but long
		// enough to tolerate a really slow test host.
		m_poSemA->Wait (TIMEOUT_COMPLETE);
	}
	void Release () {
		m_poSemB->Signal ();
	}
	void Join () {
		Release ();
		Park ();
	}
	CAsynchronous::COperation *Barrier () {
		return new CAsyncThreadControlOperation (m_poSemB, m_poSemA);
	}
};

class CTestAsyncOperation2 : public CAsynchronous::COperation {
private:
	int volatile *m_pnRunCount;
public:
	CTestAsyncOperation2 (bool bVital, int volatile *pnRunCount)
	: COperation (bVital) {
		m_pnRunCount = pnRunCount;
	}
	void Run () {
		CThread::Sleep (TIMEOUT_COMPLETE / 3);
		(*m_pnRunCount)++;
	}
};

class CTestAsyncOperation3 : public CAsynchronous::COperation {
private:
	int volatile *m_pnRun1Count;
	int m_nRun1Expect;
	int volatile *m_pnRun2Count;
	bool volatile *m_pbAssert;
public:
	CTestAsyncOperation3 (int volatile *pnRun1Count, int nRun1Expect, int volatile *pnRun2Count, bool volatile *pbAssert) {
		m_pnRun1Count = pnRun1Count;
		m_nRun1Expect = nRun1Expect;
		m_pnRun2Count = pnRun2Count;
		m_pbAssert = pbAssert;
	}
	void Run () {
		*m_pbAssert = (*m_pnRun1Count == m_nRun1Expect);
		(*m_pnRun2Count)++;
	}
};

class CThreadRecordingOperation : public CAsynchronous::COperation {
private:
	CThread::THREAD_REF volatile *m_phThread;
public:
	CThreadRecordingOperation (CThread::THREAD_REF volatile *phThread) {
		m_phThread = phThread;
	}
	void Run () {
		*m_phThread = CThread::GetThreadRef ();
	}
};

class CDummyThread : public CThread {
public:
	CDummyThread () : CThread () {
		ASSERT (Start ());
	}
	void Run () {
		Sleep (TIMEOUT_COMPLETE);
	}
};

static void BasicOperations () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	CAsyncThreadControl oAsyncThread;
	int volatile nRun1 = 0, nRun2 = 0, nRun3 = 0;
	CTestAsyncOperation1 *poRun1 = new CTestAsyncOperation1 (0, &nRun1);
	CTestAsyncOperation1 *poRun2 = new CTestAsyncOperation1 (2, &nRun2);
	CTestAsyncOperation1 *poRun3 = new CTestAsyncOperation1 (-1, &nRun3);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (poRun3));
	ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
	oAsyncThread.Join ();
	CAsynchronous::PoisonAndRelease (poCaller);
	ASSERT (nRun1 == 1);
	ASSERT (nRun2 == 3);
	ASSERT (nRun3 == 0);
}

static void PrioritizedOperations () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	CAsyncThreadControl oAsyncThread;
	bool volatile bAssert1 = false, bAssert2 = false, bAssert3 = false;
	int volatile nRun1 = 0, nRun2 = 0, nRun3 = 0;
	CTestAsyncOperation2 *poRun1 = new CTestAsyncOperation2 (false, &nRun1);
	CTestAsyncOperation3 *poRun2 = new CTestAsyncOperation3 (&nRun1, 0, &nRun2, &bAssert1);
	CTestAsyncOperation3 *poRun3a = new CTestAsyncOperation3 (&nRun1, 1, &nRun3, &bAssert2);
	CTestAsyncOperation3 *poRun3b = new CTestAsyncOperation3 (&nRun2, 1, &nRun3, &bAssert3);
	// Run an operation that will block the async-thread in the "Run" method
	ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
	// Wait for the async-thread to get blocked in the "Run" method joining the barrier
	oAsyncThread.Park ();
	// Throw operations at the queue
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun3a));
	ASSERT (poCaller->RunFirst (poRun2));
	ASSERT (poCaller->Run (poRun3b));
	// Release the async-thread to process its queue - should run jobs as 2, 1, 3a, 3b
	oAsyncThread.Release ();
	// Wait for the jobs to run to completion
	ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
	oAsyncThread.Join ();
	CAsynchronous::PoisonAndRelease (poCaller);
	ASSERT (nRun1 == 1);
	ASSERT (nRun2 == 1);
	ASSERT (nRun3 == 2);
	ASSERT (bAssert1);
	ASSERT (bAssert2);
	ASSERT (bAssert3);
}

static void VitalOperations () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	CAsyncThreadControl oAsyncThread;
	int volatile nRun1 = 0, nRun2 = 0, nRun3 = 0, nRun4 = 0, nRun5 = 0;
	CTestAsyncOperation2 *poRun1 = new CTestAsyncOperation2 (false, &nRun1);
	CTestAsyncOperation2 *poRun2 = new CTestAsyncOperation2 (false, &nRun2);
	CTestAsyncOperation2 *poRun3 = new CTestAsyncOperation2 (true, &nRun3);
	CTestAsyncOperation2 *poRun4 = new CTestAsyncOperation2 (false, &nRun4);
	CTestAsyncOperation2 *poRun5 = new CTestAsyncOperation2 (true, &nRun5);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (oAsyncThread.Barrier ())); // B1
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (poRun3));
	ASSERT (poCaller->Run (poRun4));
	ASSERT (poCaller->Run (poRun5));
	ASSERT (poCaller->Run (oAsyncThread.Barrier ())); // B2
	oAsyncThread.Park (); // B1
	CAsynchronous::PoisonAndRelease (poCaller);
	oAsyncThread.Release (); // B1
	oAsyncThread.Join (); // B2
	ASSERT (nRun1 == 1); // started before the poison
	ASSERT (nRun2 == 0); // non-vital operation skipped
	ASSERT (nRun3 == 1);
	ASSERT (nRun4 == 0);
	ASSERT (nRun5 == 1);
}

static void ThreadIdleTimeout () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	CAsyncThreadControl oAsyncThread;
	poCaller->SetTimeoutInactivity (TIMEOUT_COMPLETE / 2);
	CThread::THREAD_REF volatile hThread1 = 0, hThread2 = 0;
	CThreadRecordingOperation *poRun1 = new CThreadRecordingOperation (&hThread1);
	CThreadRecordingOperation *poRun2 = new CThreadRecordingOperation (&hThread2);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
	oAsyncThread.Join ();
	ASSERT (hThread1 == hThread2);
	poRun2 = new CThreadRecordingOperation (&hThread2);
	CThread::Sleep (TIMEOUT_COMPLETE);
	// Some O/Ss will re-use the thread ID/reference data so create a dummy thread
	CThread *poDummyThread = new CDummyThread ();
	CThread::Release (poDummyThread);
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
	oAsyncThread.Join ();
	if (hThread2) {
		ASSERT (hThread1 != hThread2);
	} else {
		LOGWARN (TEXT ("ThreadIdleTimeout test might have failed - no thread identity"));
	}
	CAsynchronous::PoisonAndRelease (poCaller);
}

static void ThreadRecycling () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	CAsyncThreadControl oAsyncThread;
	CThread::THREAD_REF volatile hThread1 = 0, hThread2 = 0;
	CThreadRecordingOperation *poRun1 = new CThreadRecordingOperation (&hThread1);
	CThreadRecordingOperation *poRun2 = new CThreadRecordingOperation (&hThread2);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
	oAsyncThread.Join ();
	if (hThread1) {
		ASSERT (poCaller->RecycleThread ());
		ASSERT (poCaller->Run (poRun2));
		ASSERT (poCaller->Run (oAsyncThread.Barrier ()));
		oAsyncThread.Join ();
		ASSERT (hThread1 != hThread2);
	} else {
		LOGWARN (TEXT ("ThreadRecycling test might have failed - no thread identity"));
	}
	CAsynchronous::PoisonAndRelease (poCaller);
}

/// Tests the functions and objects in Util/Asynchronous.cpp
BEGIN_TESTS (AsynchronousTest)
	UNIT_TEST (BasicOperations)
	UNIT_TEST (PrioritizedOperations)
	UNIT_TEST (VitalOperations)
	UNIT_TEST (ThreadIdleTimeout)
	UNIT_TEST (ThreadRecycling)
END_TESTS
