'
' Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
'
' Please see distribution for license.
'

Option Explicit

Const fDirectory = 16
Const strActionData = "CustomActionData"
Const strDataTemplate = "data-template\"
Const strFileSystemObject = "Scripting.FileSystemObject"
Const strCurrentFolder = "."
Const strParentFolder = ".."

Private Function CopyFiles (oFileSystem, strSourceDir, strDestDir)
  On Error Resume Next
  Dim oSourceDir
  Dim oFolder
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

Private Function DeleteIfEmpty (oFileSystem, strDir)
  On Error Resume Next
  Dim oDir
  Dim oFolder
  Dim oFile
  Dim strName
  Set oDir = oFileSystem.GetFolder (strDir)
  For Each oFolder In oDir.SubFolders
    strName = oFolder.Name
    If strName <> strCurrentFolder And strName <> strParentFolder Then
      DeleteIfEmpty = False
      Exit Function
    End If
  Next
  For Each oFile In oDir.Files
    DeleteIfEmpty = False
    Exit Function
  Next
  oFileSystem.DeleteFolder Left (strDir, Len (strDir) - 1)
  DeleteIfEmpty = True
End Function

Private Function FileCompare (oFileSystem, strFileA, strFileB)
  On Error Resume Next
  Dim nResult
  Dim oInputA
  Dim oInputB
  Dim strA
  Dim strB
  nResult = 0
  Set oInputA = oFileSystem.OpenTextFile (strFileA)
  Set oInputB = oFileSystem.OpenTextFile (strFileB)
  While Not oInputA.AtEndOfStream And Not oInputB.AtEndOfStream And nResult = 0
    strA = oInputA.ReadLine
    strB = oInputB.ReadLine
    If strA <> strB Then
      oInputA.Close
      oInputB.Close
      FileCompare = 1
      Exit Function
    End If
  Wend
  If oInputA.AtEndOfStream Then
    If oInputB.AtEndOfStream Then
      nResult = 0
    Else
      nResult = 1
    End If
  Else
    nResult = 1
  End If
  oInputA.Close
  oInputB.Close
  If Err.Number <> 0 Then
    nResult = -1
    Err.Clear
  End If
  FileCompare = nResult
End Function

Private Function RemoveFiles (oFileSystem, strSourceDir, strDestDir)
  On Error Resume Next
  Dim oSourceDir
  Dim oFolder
  Dim oFile
  Dim strSource
  Dim strDest
  Dim strName
  Dim nResult
  Set oSourceDir = oFileSystem.GetFolder (strSourceDir)
  If Err.Number <> 0 Then
    RemoveFiles = Err.Number
    Err.Clear
    Exit Function
  End If
  For Each oFolder In oSourceDir.SubFolders
    strName = oFolder.Name
    If strName <> strCurrentFolder And strName <> strParentFolder Then
      ' Check for removals in the folder and delete it if it's empty afterwards
      strDest = strDestDir & strName & "\"
      If oFileSystem.FolderExists (strDest) Then
        strSource = strSourceDir & strName & "\"
        nResult = RemoveFiles (oFileSystem, strSource, strDest)
	If nResult <> 0 Then
	  RemoveFiles = nResult
	  Exit Function
	End If
	DeleteIfEmpty oFileSystem, strDest
      End If
    End If
  Next
  For Each oFile In oSourceDir.Files
    strName = oFile.Name
    ' Only delete files if they match
    strDest = strDestDir & strName
    If oFileSystem.FileExists (strDest) Then
      strSource = strSourceDir & strName
      If FileCompare (oFileSystem, strSource, strDest) = 0 Then
        oFileSystem.DeleteFile strDest
        If Err.Number <> 0 Then
	  RemoveFiles = Err.Number
	  Err.Clear
	  Exit Function
	End If
      End If
    End If
  Next
  RemoveFiles = 0
End Function

Public Function RemoveSiteConfiguration (strPlatformDir, strSiteDir)
  On Error Resume Next
  Dim oFileSystem
  Dim strTemplateDir
  Set oFileSystem = CreateObject (strFileSystemObject)
  If Err.Number <> 0 Then
    RemoveSiteConfiguration = Err.Number
    Err.Clear
    Exit Function
  End If
  strTemplateDir = strPlatformDir & strDataTemplate
  If oFileSystem.FolderExists (strTemplateDir) Then
    RemoveSiteConfiguration = RemoveFiles (oFileSystem, strTemplateDir, strSiteDir)
  Else
    ' No reference template to erase files for
    RemoveSiteConfiguration = 0
  End If
End Function

Public Function RemoveSiteConfigurationAI ()
  On Error Resume Next
  Dim astrParams
  Dim strPlatformDir
  Dim strSiteDir
  astrParams = Split (Session.Property (strActionData), ";")
  strPlatformDir = astrParams (0)
  strSiteDir = astrParams (1)
  If Err.Number <> 0 Then
    RemoveSiteConfigurationAI = Err.Number
    Err.Clear
    Exit Function
  End If
  RemoveSiteConfigurationAI = RemoveSiteConfiguration (strPlatformDir, strSiteDir)
End Function
