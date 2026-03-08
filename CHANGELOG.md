# Changelog

## [1.0.0] - 2024-01-15
### Added
- Initial release of Exory File Manager
- Browse files and folders with list and grid views
- Copy, move, delete, rename operations
- Create new folders and files
- Search functionality with real-time results
- Sort files by name, size, date, and type
- View file properties and details
- Image, video, and audio file preview
- PDF and document viewer
- Archive compression and extraction (ZIP, RAR, 7Z, TAR, GZ)
- Favorites system for quick access to important locations
- Recent files tracking
- Multiple file selection with contextual action bar
- Share files with other apps
- File information and metadata display
- Storage analysis and cleanup suggestions
- Dark theme and AMOLED black theme
- Multiple language support (English, Indonesian, Arabic, Chinese, Spanish, Hindi)
- App lock with password, PIN, pattern, and biometric
- File encryption with AES-256
- Secure delete with multiple overwrite passes
- Hide sensitive files
- Root access support for system files
- FTP server for wireless file transfer
- Cloud storage integration (Google Drive, Dropbox, OneDrive)
- Auto-cleanup of temporary files, cache, and empty folders
- Thumbnail generation for images, videos, and documents
- Customizable appearance and behavior
- Batch rename with advanced options
- File conflict resolution dialog
- Undo/redo for file operations
- Background operations with notifications
- Backup and restore settings
- Comprehensive tutorial for first-time users

### Security
- Military-grade AES-256 encryption
- Secure delete with DoD 5220.22-M standard
- App lock with multiple methods
- Biometric authentication support
- Encrypted shared preferences
- Prevent screenshots in secure mode

### Performance
- Optimized for large directories with thousands of files
- Lazy loading and pagination for smooth scrolling
- Efficient thumbnail caching
- Background processing for long operations
- Memory-efficient file handling

### Compatibility
- Android 5.0 (API 21) and above
- Support for scoped storage on Android 10+
- Full file access on Android 11+ with MANAGE_EXTERNAL_STORAGE
- Support for both 32-bit and 64-bit devices
- Tablet-optimized layouts

## [1.0.1] - 2024-01-20
### Fixed
- Crash when opening certain image files
- Memory leak in thumbnail loader
- Incorrect file size calculation for folders
- Permission handling on Android 10 devices
- Search not working in subdirectories

### Added
- Option to show/hide hidden files
- Long press to select items in grid view
- Swipe to refresh in all lists
- Double tap to zoom in image viewer

### Changed
- Improved file loading speed
- Better error messages for permission issues
- Updated translations

## [1.0.2] - 2024-01-25
### Fixed
- Crash when extracting password-protected archives
- Progress bar not updating during long operations
- Navigation drawer not closing on item click
- Theme not applying correctly on Android 8.0

### Added
- Support for more archive formats (TAR.BZ2, TAR.GZ)
- Option to split archives when compressing
- Password protection for archives
- File conflict resolution dialog with preview

### Changed
- Improved archive extraction speed
- Better handling of special characters in filenames
- Updated third-party libraries

## [1.0.3] - 2024-02-01
### Fixed
- Crash when accessing USB OTG storage
- File permissions not displaying correctly
- Search not working in encrypted folders
- App lock not working after device restart

### Added
- USB OTG storage support
- Root explorer mode
- Batch rename with numbering
- File checksum calculation (MD5, SHA-1, SHA-256)
- EXIF data viewer for images

### Changed
- Improved root access detection
- Better handling of symbolic links
- Updated documentation

## [1.0.4] - 2024-02-08
### Fixed
- Memory issues when handling very large files
- FTP server disconnecting unexpectedly
- Cloud sync not working on some devices
- Pattern lock not saving correctly

### Added
- FTP server with customizable port
- Cloud storage sync with background service
- Scheduled auto-cleanup
- Backup reminder notifications
- Custom themes support

### Changed
- Improved network stability
- Better error handling for cloud operations
- Updated to latest Android SDK

## [1.0.5] - 2024-02-15
### Fixed
- Crash on Android 5.0 devices
- File operation progress not updating
- Navigation drawer lag on older devices
- Duplicate file detection false positives

### Added
- File operation queue with pause/resume
- Advanced search with filters
- Storage usage graphs
- File type statistics
- Media metadata viewer

### Changed
- Optimized for low-end devices
- Improved accessibility support
- Updated privacy policy

## [1.0.6] - 2024-02-22
### Fixed
- Security vulnerability in encryption module
- Path traversal vulnerability
- Memory leak in image viewer
- FTP server security issues

### Added
- Security patch updates
- Improved encryption with stronger algorithms
- Two-factor authentication option
- Secure folder with separate password

### Changed
- Updated security best practices
- Enhanced data protection
- Better secure delete implementation

## [1.0.7] - 2024-03-01
### Fixed
- Crash when opening certain PDF files
- Thumbnail generation for large videos
- File conflict dialog not showing correctly
- Auto-cleanup not respecting user settings

### Added
- PDF viewer with text selection
- Video player with subtitle support
- Audio player with playlist
- Text editor with syntax highlighting
- Code viewer with line numbers

### Changed
- Improved media player performance
- Better PDF rendering
- Updated third-party libraries

## [1.0.8] - 2024-03-08
### Fixed
- Root access not working on some devices
- Cloud sync conflicts not resolved properly
- Backup restore failing on Android 12
- Notification channel issues on Android 8+

### Added
- Root file operations with proper permissions
- Cloud sync conflict resolution
- Backup encryption option
- Android 12+ Material You theming
- Dynamic color support

### Changed
- Improved root access reliability
- Better cloud sync performance
- Updated to Material Design 3

## [1.0.9] - 2024-03-15
### Fixed
- Crash when opening certain archive files
- Memory issues with very large directories
- App lock not working after update
- FTP server connection drops

### Added
- Archive preview without extraction
- File recovery for recently deleted items
- Disk usage analyzer with visual charts
- File duplication finder
- Large file finder

### Changed
- Improved archive handling
- Better memory management
- Updated file recovery system

## [1.0.10] - 2024-03-22
### Fixed
- Security vulnerability in file provider
- Permission handling on Android 13
- Backup not including all settings
- Theme switching issues

### Added
- Android 13 support
- Notification permission handling
- Photo picker integration
- Media store integration
- File picker API improvements

### Changed
- Updated target SDK to 33
- Improved scoped storage handling
- Better permission management

## [1.1.0] - 2024-04-01
### Added
- SMB/CIFS network shares support
- WebDAV client
- SFTP/SSH file transfer
- Remote file browser
- Network drive mapping
- Offline mode for cloud files
- File versioning for cloud storage
- Collaborative editing
- File locking
- Activity log with filters

### Fixed
- Various stability issues
- Performance optimizations
- Memory leaks

### Changed
- Major architecture overhaul
- Updated to latest libraries
- Improved UI/UX

## [1.1.1] - 2024-04-08
### Fixed
- Network share connection issues
- Offline mode not syncing correctly
- File versioning conflicts
- Activity log not updating

### Added
- Network share bookmarks
- Quick connect for recent servers
- Transfer queue management
- Bandwidth limiting option

### Changed
- Improved network stability
- Better error handling
- Updated documentation

## [1.1.2] - 2024-04-15
### Fixed
- Crash on some network operations
- Memory leak in transfer queue
- UI freezes during large transfers
- Notification not updating

### Added
- Transfer speed display
- Estimated time remaining
- Pause/resume for transfers
- Background transfer support

### Changed
- Optimized network operations
- Improved transfer performance
- Better battery usage

## [1.1.3] - 2024-04-22
### Fixed
- Security issues in network protocols
- Authentication problems with some servers
- File permission errors on network shares
- Sync conflicts in collaborative editing

### Added
- SSH key authentication
- Kerberos support for SMB
- File locking with timeout
- Conflict resolution wizard

### Changed
- Enhanced security
- Better authentication handling
- Updated encryption protocols

## [1.1.4] - 2024-04-29
### Fixed
- Crash when accessing certain network paths
- Memory issues with large directory listings
- Transfer queue not persisting after restart
- Activity log filtering issues

### Added
- Network discovery (SMB, FTP)
- Quick access to nearby devices
- Transfer history
- Bandwidth usage statistics

### Changed
- Improved network discovery
- Better queue management
- Updated UI for transfers

## [1.1.5] - 2024-05-06
### Fixed
- Connection drops on unstable networks
- File corruption in large transfers
- Authentication timeout issues
- Proxy configuration problems

### Added
- Connection retry with exponential backoff
- Checksum verification for transfers
- Proxy support (HTTP, SOCKS)
- VPN detection and handling

### Changed
- Improved connection stability
- Better error recovery
- Updated network stack

## [1.1.6] - 2024-05-13
### Fixed
- Security vulnerabilities in network protocols
- Memory leak in connection pool
- UI thread blocking during network ops
- Notification issues on Android 14

### Added
- Android 14 support
- Predictive back gesture support
- Credential manager integration
- Passkey support

### Changed
- Updated target SDK to 34
- Improved credential handling
- Better gesture navigation

## [1.1.7] - 2024-05-20
### Fixed
- Crash on Android 14 devices
- Network share discovery on some networks
- File permission inheritance issues
- Sync folder selection problems

### Added
- Advanced network settings
- Custom port configuration
- Connection profiles
- Network speed test

### Changed
- Improved network performance
- Better device compatibility
- Updated third-party libraries

## [1.1.8] - 2024-05-27
### Fixed
- Security issues in file transfer
- Authentication token expiration handling
- Concurrent transfer conflicts
- Network timeout configuration

### Added
- End-to-end encryption for transfers
- Two-factor authentication for network shares
- Transfer encryption indicator
- Security audit log

### Changed
- Enhanced transfer security
- Better authentication flow
- Updated security protocols

## [1.1.9] - 2024-06-03
### Fixed
- Crash when opening network locations
- Memory usage in directory browser
- Search not working in network paths
- Bookmark synchronization issues

### Added
- Network path search
- Advanced search filters for network
- Bookmark sync across devices
- Cloud backup for settings

### Changed
- Improved network performance
- Better search algorithm
- Updated sync engine

## [1.2.0] - 2024-06-10
### Added
- Plugin system for extensibility
- Custom actions and scripts
- Macro recorder for repetitive tasks
- Task scheduler
- Workflow automation
- Custom themes and skins
- Icon packs support
- Language packs
- Developer API
- Web interface for remote management

### Fixed
- Various bugs and issues
- Performance bottlenecks
- Memory leaks

### Changed
- Major architecture update
- Plugin API finalized
- Documentation updated

## [1.2.1] - 2024-06-17
### Fixed
- Plugin compatibility issues
- Script execution security
- Task scheduler reliability
- Macro recording accuracy

### Added
- Plugin marketplace
- Script editor with syntax highlighting
- Task templates
- Macro library

### Changed
- Improved plugin system
- Better script sandboxing
- Updated documentation

## [1.2.2] - 2024-06-24
### Fixed
- Security vulnerabilities in plugin system
- Resource leaks in long-running tasks
- Task dependency resolution
- Macro playback timing

### Added
- Plugin sandboxing
- Resource limits for plugins
- Task dependencies
- Conditional execution

### Changed
- Enhanced security
- Better resource management
- Updated plugin API

## [1.2.3] - 2024-07-01
### Fixed
- Crash on some plugin operations
- Memory issues in task scheduler
- UI freezes during macro playback
- Theme switching with plugins

### Added
- Plugin debugging tools
- Task monitoring
- Macro variables and loops
- Custom UI for plugins

### Changed
- Improved plugin stability
- Better debugging support
- Updated documentation

## [1.2.4] - 2024-07-08
### Fixed
- Plugin installation issues
- Permission handling for scripts
- Task scheduling accuracy
- Macro recording for complex operations

### Added
- Plugin signing and verification
- Script permission system
- Task priorities
- Macro conditions and branches

### Changed
- Enhanced security
- Better permission management
- Updated plugin format

## [1.2.5] - 2024-07-15
### Fixed
- Performance issues with multiple plugins
- Memory leaks in script engine
- Task queue management
- Macro compatibility

### Added
- Plugin performance metrics
- Script optimization tools
- Task notifications
- Macro sharing

### Changed
- Improved plugin performance
- Better memory management
- Updated engine

## [1.2.6] - 2024-07-22
### Fixed
- Security issues in script execution
- Plugin update problems
- Task persistence after restart
- Macro recording for file operations

### Added
- Script sandbox with resource limits
- Plugin auto-update
- Task history
- Macro export/import

### Changed
- Enhanced script security
- Better update mechanism
- Updated persistence

## [1.2.7] - 2024-07-29
### Fixed
- Crash on Android 15 preview
- Plugin compatibility with new Android
- Task scheduling on Doze mode
- Macro performance

### Added
- Android 15 support
- Predictive back gesture for plugins
- Edge-to-edge display support
- Material You theming for plugins

### Changed
- Updated for Android 15
- Improved gesture handling
- Better theming support

## [1.2.8] - 2024-08-05
### Fixed
- Network issues in Android 15
- Plugin permission handling
- Task execution on battery saver
- Macro recording accuracy

### Added
- Android 15 specific features
- Private space support
- Partial screen sharing
- Enhanced media controls

### Changed
- Optimized for Android 15
- Updated target SDK to 35
- Improved battery efficiency

## [1.2.9] - 2024-08-12
### Fixed
- Security vulnerabilities in Android 15
- Plugin sandbox bypass
- Task scheduling conflicts
- Macro data loss

### Added
- Android 15 security features
- Enhanced credential manager
- Passkey support for plugins
- Biometric prompt improvements

### Changed
- Updated security for Android 15
- Better credential handling
- Improved authentication

## [1.3.0] - 2024-08-19
### Added
- AI-powered file organization
- Smart folders with rules
- Content-based search
- Duplicate detection with AI
- File classification
- Intelligent cleanup suggestions
- Predictive file access
- Usage analytics
- Personalized recommendations
- Voice commands
- Natural language search
- Automated workflows with AI

### Fixed
- Long-standing bugs
- Performance issues
- User experience problems

### Changed
- Major AI integration
- Architecture update for ML
- New ML models
