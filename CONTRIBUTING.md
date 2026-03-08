# Contributing to Exory File Manager

First off, thank you for considering contributing to Exory File Manager! It's people like you that make Exory File Manager such a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues list as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples.
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include screenshots and animated GIFs** which show you following the described steps and clearly demonstrate the problem.
* **If the problem is related to performance or memory**, include a CPU profile capture with your report.
* **If the problem wasn't triggered by a specific action**, describe what you were doing before the problem happened.

### Suggesting Enhancements

If you have a suggestion for a new feature or enhancement, we'd love to hear it! Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps** or point to similar features in other apps.
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Include screenshots and animated GIFs** which help demonstrate the steps or point out the part of Exory File Manager which the suggestion is related to.
* **Explain why this enhancement would be useful** to most Exory File Manager users.
* **List some other file managers where this enhancement exists.**

### Pull Requests

* Fill in the required template
* Do not include issue numbers in the PR title
* Follow the Android/Kotlin styleguides
* Include screenshots and animated GIFs in your pull request whenever possible
* Follow the Material Design guidelines
* Document new code
* End all files with a newline

## Styleguides

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line
* Consider starting the commit message with an applicable emoji:
    * 🎨 `:art:` when improving the format/structure of the code
    * 🐎 `:racehorse:` when improving performance
    * 🚱 `:non-potable_water:` when plugging memory leaks
    * 📝 `:memo:` when writing docs
    * 🐧 `:penguin:` when fixing something on Linux
    * 🍎 `:apple:` when fixing something on macOS
    * 🏁 `:checkered_flag:` when fixing something on Windows
    * 🐛 `:bug:` when fixing a bug
    * 🔥 `:fire:` when removing code or files
    * 💚 `:green_heart:` when fixing the CI build
    * ✅ `:white_check_mark:` when adding tests
    * 🔒 `:lock:` when dealing with security
    * ⬆️ `:arrow_up:` when upgrading dependencies
    * ⬇️ `:arrow_down:` when downgrading dependencies
    * 👕 `:shirt:` when removing linter warnings

### Kotlin Styleguide

All Kotlin must adhere to [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).

* Use 4 spaces for indentation (no tabs)
* Use meaningful variable names
* Follow the naming conventions:
    * Classes: `PascalCase`
    * Functions: `camelCase`
    * Constants: `SCREAMING_SNAKE_CASE`
    * Properties: `camelCase`
* Add documentation for public APIs
* Use Kotlin's null safety features
* Prefer `val` over `var`
* Use data classes for POJOs
* Use sealed classes for restricted class hierarchies
* Use extension functions where appropriate
* Use coroutines for asynchronous operations

### XML Styleguide

* Use 4 spaces for indentation (no tabs)
* Use meaningful IDs (e.g., `@+id/btn_submit` not `@+id/button1`)
* Order attributes:
    1. `android:id`
    2. `android:layout_width`, `android:layout_height`
    3. `style` / `android:theme`
    4. Other `android:layout_*` attributes
    5. Other `android:*` attributes in alphabetical order
    6. Custom attributes (`app:`, `tools:`)
* Use `tools:` attributes for sample data
* Extract strings to `strings.xml`
* Extract dimensions to `dimens.xml`
* Extract colors to `colors.xml`
* Extract styles to `styles.xml`

### Documentation Styleguide

* Use [KDoc](https://kotlinlang.org/docs/reference/kotlin-doc.html) for documentation
* Document all public APIs
* Include `@param` and `@return` tags where appropriate
* Include sample code in documentation
* Keep comments up to date with code changes

## Additional Notes

### Issue and Pull Request Labels

| Label name | Description |
| --- | --- |
| `bug` | Confirmed bugs or reports that are likely to be bugs |
| `enhancement` | Feature requests |
| `help-wanted` | Extra attention is needed |
| `good-first-issue` | Good for newcomers |
| `question` | Questions more than bug reports or feature requests |
| `duplicate` | Issues which are duplicates of other issues |
| `wontfix` | Issues that won't be fixed |
| `invalid` | Issues which aren't valid |
| `documentation` | Documentation only changes |
| `performance` | Performance related issues |
| `security` | Security related issues |
| `dependencies` | Dependency updates |
| `android-version` | Issues related to specific Android versions |
| `device-specific` | Issues specific to certain devices |
| `translation` | Translation related issues |
| `ui` | User interface related issues |
| `ux` | User experience related issues |

## Development Setup

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/ExoryFileManager.git
```

1. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/Exory550/ExoryFileManager.git
   ```
2. Open the project in Android Studio
3. Build and run the app

Building

· Debug build: ./gradlew assembleDebug
· Release build: ./gradlew assembleRelease
· Run tests: ./gradlew test
· Run lint: ./gradlew lint

Testing

· Write unit tests for new functionality
· Write instrumentation tests for UI changes
· Ensure all tests pass before submitting PR
· Test on multiple Android versions and devices
· Test with different screen sizes and orientations

Internationalization

· Use string resources for all user-facing text
· Add translations in values-* directories
· Keep strings concise and clear
· Use placeholders for dynamic content

Security

· Never commit sensitive information (keys, passwords, etc.)
· Use Android Keystore for cryptographic operations
· Follow security best practices
· Report security vulnerabilities privately

License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

Questions?

Feel free to contact the maintainers if you have any questions:

· Email: support@exory.official55@gmail.com
· GitHub: @Exory550

Thank you for contributing! 🎉
