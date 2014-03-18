'
' Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
'
' Please see distribution for license.
'

Const strActionData = "CustomActionData"

Private Function ReadRegCredential (oWSH, strKey)
  On Error Resume Next
  Dim strValue
  strValue = oWSH.RegRead ("HKLM\SOFTWARE\Wow6232Node\" & strKey)
  If Err.Number = 0 Then
    ReadRegCredential = strValue
  Else
    Err.Clear
    strValue = oWSH.RegRead ("HKLM\SOFTWARE\" & strKey)
    If Err.Number = 0 Then
      RegReadCredential = strValue
    Else
      RegReadCredential = "Error " & Err.Number
      Err.Clear
    End If
  End If
End Function

Public Function CredentialsAI ()
  On Error Resume Next
  Dim oWSH
  Set oWSH = CreateObject ("WScript.Shell")
  If Err.Number <> 0 Then
    CredentialsAI = Err.Number
    Err.Clear
    Exit Function
  End If
  Session ("OPENGAMMA_USER") = ReadRegCredential (oWSH, "OpenGammaLtd\User\")
  Session ("OPENGAMMA_PASSWORD") = ReadRegCredential (oWSH, "OpenGammaLtd\User\Password\")
  If Err.Number <> 0 Then
    CredentialsAI = Err.Number
    Err.Clear
  Else
    CredentialsAI = 0
  End If
End Function
