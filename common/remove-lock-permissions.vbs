' Processes an MSI to remove the LockPermissions table erroneously inserted by Advanced
' Installer when the MsiLockPermissionsEx table is used.

Option Explicit

Const msiOpenDatabaseModeTransact = 1

Dim installer, database, filename, stmt

filename = WScript.Arguments (0)
Set installer = CreateObject ("WindowsInstaller.Installer")
Set database = installer.OpenDatabase (filename, msiOpenDatabaseModeTransact)
Set stmt = database.OpenView ("DROP TABLE LockPermissions")
stmt.Execute
database.Commit
