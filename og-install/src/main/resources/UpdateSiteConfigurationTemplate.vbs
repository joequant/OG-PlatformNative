'
' Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
'
' Please see distribution for license.
'

Const fDirectory = 16
Const strActionData = "CustomActionData"
Const strDataTemplate = "data-template\"
Const strFileSystemObject = "Scripting.FileSystemObject"
Const strCurrentFolder = "."
Const strParentFolder = ".."

Private Function CopyFiles (oFileSystem, strSourceDir, strDestDir)
  On Error Resume Next
  Dim oSourceDir
  Dim oFile
  Dim strSource
  Dim strDest
  Dim strName
  Dim nResult
  Set oSourceDir = oFileSystem.GetFolder (strSourceDir)
  If Err.Number <> 0 Then
    CopyFiles = Err.Number
    Err.Clear
    Exit Function
  End If
  For Each oFolder In oSourceDir.SubFolders
    strName = oFolder.Name
    If strName <> strCurrentFolder And strName <> strParentFolder Then
      ' Create matching target folder if not present and recurse to copy files
      strSource = strSourceDir & strName & "\"
      strDest = strDestDir & strName & "\"
      If Not oFileSystem.FolderExists (strDest) Then
        oFileSystem.CreateFolder (strDest)
	If Err.Number <> 0 Then
	  CopyFiles = Err.Number
	  Err.Clear
	  Exit Function
	End If
      End If
      nResult = CopyFiles (oFileSystem, strSource, strDest)
      If nResult <> 0 Then
        CopyFiles = nResult
	Exit Function
      End If
    End If
  Next
  For Each oFile In oSourceDir.Files
    strName = oFile.Name
    ' Only write individual files that do not exist
    strDest = strDestDir & strName
    If Not oFileSystem.FileExists (strDest) Then
      oFile.Copy strDest
      If Err.Number <> 0 Then
        CopyFiles = Err.Number
	Err.Clear
	Exit Function
      End If
    End If
  Next
  CopyFiles = 0
End Function

Public Function UpdateSiteConfiguration (strPlatformDir, strSiteDir)
  On Error Resume Next
  Dim oFileSystem
  Dim strTemplateDir
  Set oFileSystem = CreateObject (strFileSystemObject)
  If Err.Number <> 0 Then
    UpdateSiteConfiguration = Err.Number
    Err.Clear
    Exit Function
  End If
  strTemplateDir = strPlatformDir & strDataTemplate
  If oFileSystem.FolderExists (strTemplateDir) Then
    UpdateSiteConfiguration = CopyFiles (oFileSystem, strTemplateDir, strSiteDir)
  Else
    ' No template to copy
    UpdateSiteConfiguration = 0
  End If
End Function

Public Function UpdateSiteConfigurationAI ()
  On Error Resume Next
  Dim astrParams
  Dim strPlatformDir
  Dim strSiteDir
  Dim strProp
  strProp = Session.Property (strActionData)
  astrParams = Split (Session.Property (strActionData), ";")
  strPlatformDir = astrParams (0)
  strSiteDir = astrParams (1)
  If Err.Number <> 0 Then
    UpdateSiteConfigurationAI = Err.Number
    Err.Clear
    Exit Function
  End If
  UpdateSiteConfigurationAI = UpdateSiteConfiguration (strPlatformDir, strSiteDir)
End Function
