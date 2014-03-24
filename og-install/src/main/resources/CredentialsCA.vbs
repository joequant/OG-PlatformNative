'
' Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
'
' Please see distribution for license.
'

Const strActionData = "CustomActionData"
Const errMask = 65535
Const errServiceNotFound = 1243
Const errAccessDenied = 5
Const strSpecial = "!$%^&*()_+-=[]{};#:@~\|,./<>?"

Dim strUsername
Dim strPassword

Private Function ReadRegCredential (oWSH, strKey)
  On Error Resume Next
  Dim strValue
  Dim nError
  strValue = oWSH.RegRead ("HKLM\SOFTWARE\Wow6432Node\" & strKey)
  If Err.Number = 0 Then
    ReadRegCredential = strValue
  Else
    nError = Err.Number And errMask
    Err.Clear
    If nError <> errAccessDenied Then
      strValue = oWSH.RegRead ("HKLM\SOFTWARE\" & strKey)
      If Err.Number = 0 Then
        ReadRegCredential = strValue
      Else
        ReadRegCredential = "Error " & (Err.Number And errMask)
        Err.Clear
      End If
    Else
      ReadRegCredential = "Error " & nError
    End If
  End If
End Function

Private Sub ReadRegCredentials ()
  Dim oWSH
  If IsEmpty (strUsername) Or IsEmpty (strPassword) Then
    Set oWSH = CreateObject ("WScript.Shell")
    strUsername = ReadRegCredential (oWSH, "OpenGammaLtd\User\")
    strPassword = ReadRegCredential (oWSH, "OpenGammaLtd\User\Password\")
    If Left (strUsername, 6) = "Error " Then
      Err.Raise CInt (Mid (strUsername, 6))
    Else
      If InStr (strUsername, "\") = 0 And InStr (strUsername, "@") = 0 Then
        strUsername = ".\" & strUsername
      End If
    End If
    If Left (strPassword, 6) = "Error " Then
      Err.Raise CInt (Mid (strPassword, 6))
    End If
  End If
End Sub

Private Function EscapeSQL (strValue)
  On Error Resume Next
  strValue = Replace (strValue, "'", "''")
  EscapeSQL = strValue
End Function

Public Function SetServiceUser (strService)
  On Error Resume Next
  Dim oWMI
  Dim aoService
  Dim oService
  Dim nError
  ReadRegCredentials
  If Err.Number <> 0 Then
    SetServiceUser = Err.Number
    Err.Clear
    Exit Function
  End If
  Set oWMI = GetObject ("winmgmts:\\.\root\cimv2")
  Set aoService = oWMI.ExecQuery ("SELECT * FROM Win32_Service WHERE Name='" & EscapeSQL (strService) & "'")
  If Err.Number <> 0 Then
    SetServiceUser = Err.Number
    Err.Clear
    Exit Function
  End If
  If aoService.Count = 0 Then
    SetServiceUser = errServiceNotFound
    Exit Function
  End If
  For Each oService In aoService
    nError = oService.Change (,,,,,, strUsername, strPassword)
    If nError <> 0 Then
      SetServiceUser = nError - 1073741824
      Exit Function
    End If
  Next
  SetServiceUser = 0
End Function

Public Function SetServiceUserAI ()
  On Error Resume Next
  Dim strService
  strService = Session.Property (strActionData)
  If Err.Number <> 0 Then
    SetServiceUserAI = Err.Number
    Err.Clear
    Exit Function
  End If
  SetServiceUserAI = SetServiceUser (strService)
End Function

Public Function GeneratePassword ()
  On Error Resume Next
  Dim strPassword
  Dim n
  Dim c
  Randomize
  strPassword = ""
  For n = 1 To 3
    strPassword = strPassword & Chr (CInt (Rnd () * 26) + Asc ("A"))
    strPassword = strPassword & Chr (CInt (Rnd () * 10) + Asc ("0"))
    strPassword = strPassword & Chr (CInt (Rnd () * 26) + Asc ("a"))
    strPassword = strPassword & Mid (strSpecial, CInt (Rnd () * Len (strSpecial)) + 1, 1)
  Next
  If Err.Number <> 0 Then
    GeneratePassword = "Error " & Err.Number
    Err.Clear
  Else
    GeneratePassword = strPassword
  End If
End Function

Public Function GeneratePasswordAI ()
  On Error Resume Next
  Dim strProperty
  strProperty = Session.Property (strActionData)
  If Err.Number <> 0 Then
    GeneratePasswordAI = Err.Number
    Err.Clear
    Exit Function
  End If
  Session.Property (strProperty) = GeneratePassword ()
  GeneratePasswordAI = 0
End Function
