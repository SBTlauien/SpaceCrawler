#SpaceCrawler

This is an Android Web Crawler that currently allows you to whitelist/blacklist/flag specific strings, within the URLs and source code, that it crawls.

Upon first running the app, it will create a directory within the Internal Storage names "SpaceCrawler".  That directory will contain six files, "WhiteList.txt", "BlackList.txt", "LinkWatchList.txt", "SourceWatchList.txt", "LinkResults.txt", and "SourceResults.txt".  The first four of these files can be viewed and edited within the "Options" section of the app.  Each line of these files represents a string that is either blacklisted/whitelisted/watchedFor.  Results can be viewed from the main activity of the app and can be cleared by long pressing the "VIEW RESULTS" button.

You have to click the "USE" option box for the app to use the blacklist/whitelist/watchFor/crawlExternal options.

Issues: Crashes due to memory ¯\\_(ツ)_/¯
