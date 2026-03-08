# Frequently Asked Questions

## General Questions

### What is Exory File Manager?
Exory File Manager is a powerful, feature-rich file manager for Android devices. It provides comprehensive file management capabilities including browsing, copying, moving, deleting, renaming, compressing, extracting, and securing files.

### Is Exory File Manager free?
Yes, Exory File Manager is completely free and open-source. There are no hidden costs or in-app purchases.

### What Android versions are supported?
Exory File Manager supports Android 5.0 (API 21) and above, including the latest Android 14 and 15.

### Is there a desktop version?
Currently, Exory File Manager is only available for Android devices. However, you can access your files remotely using the built-in FTP server or web interface.

## Features

### What file operations are supported?
- Browse files and folders
- Copy, move, delete, rename
- Create new folders and files
- Search for files
- Sort by name, size, date, type
- View file properties
- Share files
- Compress and extract archives (ZIP, RAR, 7Z, TAR, GZ)
- Encrypt and decrypt files
- Secure delete
- Hide sensitive files
- Root access operations
- FTP server
- Cloud storage integration
- Network shares (SMB, FTP, SFTP)

### Can I access files on external SD cards?
Yes, Exory File Manager has full support for external SD cards on all Android versions. On Android 11 and above, you need to grant "All files access" permission.

### How do I access cloud storage?
Go to Navigation drawer → Cloud storage → Add account. You can connect Google Drive, Dropbox, OneDrive, and more.

### How do I use the FTP server?
Go to Navigation drawer → FTP server → Start server. You can then connect from any FTP client using the displayed address and port.

### Can I password-protect the app?
Yes, go to Settings → Security → App lock. You can set a password, PIN, pattern, or use biometric authentication.

### How do I encrypt files?
Select files → Tap the menu (three dots) → Encrypt. You'll be prompted to set a password.

### What encryption algorithms are used?
- AES-256 for file encryption
- ChaCha20-Poly1305 for performance-critical operations
- PBKDF2 for password-based key derivation

### How does secure delete work?
Secure delete overwrites files multiple times with random data before deletion, making recovery impossible. You can configure the number of passes in Settings → Security.

## Troubleshooting

### Why can't I access external storage on Android 11+?
Android 11 introduced scoped storage. You need to grant "All files access" permission:
1. Go to Settings → Apps → Exory File Manager → Permissions
2. Tap "Files and media" → "Allow management of all files"
3. Alternatively, go to Settings → Security → Storage permission

### The app crashes when opening certain files
This could be due to:
- Corrupted files
- Insufficient memory
- Unsupported file format

Try clearing the app cache: Settings → Apps → Exory File Manager → Storage → Clear cache

### Why are some files not showing?
Make sure "Show hidden files" is enabled in Settings → Appearance. Some files might also be protected by system permissions.

### The FTP server isn't working
Check that:
- You're on the same network
- The port isn't blocked by firewall
- No other app is using the same port
- You've granted the necessary permissions

### Cloud sync is failing
Common causes:
- Invalid credentials
- Network issues
- Quota exceeded
- Rate limiting

Try removing and re-adding the account.

### The app is slow with many files
Exory File Manager is optimized for large directories, but you can:
- Disable thumbnails in Settings → Appearance
- Increase thumbnail cache size
- Use list view instead of grid view
- Close other apps to free memory

### How do I recover accidentally deleted files?
Check if you have file recovery enabled in Settings → Storage. Deleted files are moved to the recovery folder for 30 days.

## Security

### Is my data safe?
Yes, Exory File Manager takes security seriously:
- No data collection
- Open-source code
- Local encryption only
- No internet access without permission

### Can I use Exory File Manager in a corporate environment?
Yes, Exory File Manager includes enterprise features:
- Device admin integration
- Managed configuration
- Work profile support
- Audit logging

### How are passwords stored?
Passwords are hashed using PBKDF2 with a random salt. They're never stored in plain text.

### Is two-factor authentication supported?
Yes, you can enable 2FA for cloud accounts and network shares that support it.

## Development

### Is Exory File Manager open source?
Yes, the source code is available on GitHub under the Apache License 2.0.

### How can I contribute?
Check the CONTRIBUTING.md file for guidelines. You can help with:
- Code contributions
- Bug reports
- Feature suggestions
- Translations
- Documentation

### How do I build from source?
1. Clone the repository
2. Open in Android Studio
3. Build and run

### Can I create plugins?
Yes, Exory File Manager has a plugin system. Check the developer documentation for details.

## Support

### How do I report a bug?
Open an issue on GitHub with:
- Device model
- Android version
- App version
- Steps to reproduce
- Screenshots (if applicable)

### How do I request a feature?
Open an issue on GitHub with:
- Clear description
- Use case
- Expected behavior

### Where can I get help?
- GitHub Issues
- Email: support@exory550.com
- XDA Developers forum
- Reddit: r/ExoryFileManager

### How do I translate the app?
Translations are managed through Weblate. Contact us to become a translator.

## Updates

### How often is the app updated?
We release updates every 1-2 weeks with bug fixes and new features.

### How do I get beta versions?
Join the beta program on Google Play or download from GitHub releases.

### Can I downgrade to an older version?
Yes, you can install older APKs from GitHub releases. Clear app data after downgrading.

## Privacy

### What data does Exory File Manager collect?
Exory File Manager collects no personal data. Analytics can be disabled in settings.

### Does the app have internet access?
Internet access is only used for:
- Cloud storage sync (if enabled)
- Update checks
- FTP server (local network only)

You can revoke internet permission in App Info.

### Are files uploaded to the cloud?
No, files are only uploaded to cloud storage if you explicitly choose to sync them.

## Miscellaneous

### Can I change the app icon?
Yes, go to Settings → Appearance → App icon. Choose from built-in icon packs or install custom ones.

### Does it support themes?
Yes, you can choose from Light, Dark, AMOLED, and System themes. Android 12+ supports dynamic colors.

### Can I backup my settings?
Yes, go to Settings → Backup → Backup settings. You can restore on the same or another device.

### Is there a widget?
Yes, Exory File Manager includes home screen widgets for quick access to favorite locations.

### Can I use it on Android TV?
While not optimized for TV, it works with a mouse. A TV-optimized version is planned.

### Does it support Chromebook?
Yes, Exory File Manager works great on Chromebooks with full keyboard and mouse support.

### How do I uninstall?
Uninstall like any other app. Note that encrypted files will be lost unless decrypted first.

## Known Issues

### Android 15 Preview
Some features may not work correctly on Android 15 preview builds. We're working on compatibility.

### Samsung Secure Folder
Files in Samsung Secure Folder cannot be accessed due to Samsung's proprietary encryption.

### Android for Work
Work profile files are accessible but may have additional restrictions.

### Root Access
Root features require Magisk or SuperSU. Not all devices are supported.

---

**Still have questions?** Contact us at support@exory550.com or open an issue on GitHub.
