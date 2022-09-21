# Chat Success Rates

![icon](icon.png)

## Info
Track skilling success rates with game chat messages.  
The success rate of most skilling actions in-game depends on your current skill level. This plugin can be used to estimate the success rate of a skilling action that returns a message for both success and failure by counting the frequency of each result (suffix in parentheses) per level (prefix before colon). The resulting success rate data can be exported to your clipboard by right-clicking the chat message interface and selecting `Copy Chat success rates`.

![illustration-custom](https://user-images.githubusercontent.com/53493631/156419148-a346287f-d7ca-4644-95db-7af5ca925631.png)

This plugin also comes with a few integrated presets to track selected skilling actions:

![illustration-preset](https://user-images.githubusercontent.com/53493631/191285989-e5220229-729d-42a6-a5ee-f974caeb4f0a.png)

## Config options
![config](https://user-images.githubusercontent.com/53493631/156419218-3ffd6c9e-0e51-4fd3-a523-adffa7e6975c.png)
- Add level prefix: ✅ `true`
  - Whether to add a skill level prefix/identifier to the tracked chat messages
- Use boosted level: ✅ `true`
  - Whether to use the boosted skill level (numerator) or the static base skill level (denominator)
- Level prefix: `Overall`
  - The skill level prefix/identifier on the tracked chat messages
- Success message: `        `
  - The chat message used to track a skilling action success
- Failure message: `        `
  - The chat message used to track a skilling action failure
