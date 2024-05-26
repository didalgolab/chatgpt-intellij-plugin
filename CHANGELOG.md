<!-- Keep a Changelog guide -> https://keepachangelog.com -->

## [Unreleased]

## [0.2.12]
+ Breaking Change: The plugin no longer retrieves API key from the OPENAI_API_KEY environment variable. For enhanced security, please enter your API key directly into the plugin's settings.
+ Added support for new model: gpt-4-turbo-preview
+ Enabled compatibility with IDE 241.* versions.

## [0.2.11]
+ Added support for new models: gpt-4-1106-preview and gpt-3.5-turbo-1106.
+ Enabled compatibility with IDE 233.* versions.

## [0.2.10]
+ Change assistant background color for dark mode (#6)
+ Fix: java.lang.IllegalArgumentException on files without extension (#7)

## [0.2.9]
+ Fix: The context snippet editor/viewer has been made read-only.
+ Fix: The issue with the custom server URL not working has been resolved (#3).
+ Fix: Code selections already included in prompts are no longer sent in chat messages.
+ Feature: Context code snippets in chat prompts are now collapsed by default for visual efficiency.
+ Feature: After using the "ChatGPT: Add to Context" action, the prompt text field now automatically gains focus.

## [0.2.8]
+ Multiple bugfixes
+ Added setting to disable welcome message in the chat window
+ Added support for multiple contexts

With this new feature, users can select and include several code snippets from different files in a single chat prompt. A code snippet can be incorporated into the context by following these steps:

- Highlight a section of code within the editor, right-click on it, and select the "ChatGPT: Add to Context" option.
- If you wish to add the entire file to the context, simply choose the "ChatGPT: Add to Context" option without highlighting any code within the editor.
- Content of _document popups_ can be also added as a context. Just click the "More" button located in the bottom-right corner on the popup and select "ChatGPT: Add to Context" action.
- Multiple code snippets can be either added or removed before forwarding all snippets and a prompt to the chatbot.

## [0.2.7]
+ Added support for newest OpenAI '0613 models. Available now for selection from the Plugin Settings.

## [0.2.6]
+ Initial release.
