# ArcheryTrainingTimer
[![license](http://img.shields.io/github/license/schmouk/archerytrainingtimer.svg?style=plastic&labelColor=blueviolet&color=lightblue)](https://github.com/schmouk/archerytrainingtimer/license)  [![Latest release](http://img.shields.io/github/release/schmouk/archerytrainingtimer.svg?style=plastic&labelColor=blueviolet&color=success)](https://github.com/schmouk/archerytrainingtimer/releases)

A small Android application that provides timers for Archery Training


---
## Rationale
Archery activity involves timers. Shooting during competitons is time constrained, for instance. Training sessions may be time constrained also.  
This small Android application aims at providing timers for some of these activities.

A first implemented scenario is a training session organized as series of repetitions of exercises. This is the **Series Training Timer** scenario. It is currently under development process - see branch `release-0-1`.

A second scenario, not yet implemented, is a timed simulation of individual duels: **Archery Duels Simulator**.


---
## User documentation

When launching the app a single screen shows.

The first thing to do is to select there the options for the training session:
* duration of each repetition  
you will be helped to not do successive exercises at a faster or slower pace, beeps and display will help you
* number of repetitions in each series
* number of series in the training session.

You may choose to save these preferences by clicking a check box.

Once choices are done you can "Start" the countdown of the first repetition duration. A beep sound is played at the beginning of the repetition. Once completed the duration countdown is reset to its starting value, a beep is played to inform you of the beginning of next repetition and a red circle arc appears to show your progression within the current series of repetition.

You may stop the duration countdown at whish with button "Stop", then "Start" it again to resume countdown. This is a single toggle button whose text changes according to your last click opn it.

Once any series (but the last one of the session) is completed, two beeps are played and a resting time countdown is started. You cannot stop it. The resting time is forced to be 50% of the series duration.

Before the resting period ends, two beeps are played again (7 seconds before completion) to warn you that the next series will be starting. Once the resting period completes, the first repetition of next series begins, a beep sound is played and duration countdown starts.

When the whole training session completes, a dedicated sound is played and the countdown screen is dimmed with all countdowns set to 0.

Countdowns are displayed in two circles with a colored border (yellow).
* The biggest one shows time countdown for every repetition. The progression of repetitions in a series is shown with a growing circle arc of a different color (red) superimposed to the yellow border.
* the smaller circle shows series countdown.

When resting time raises, the biggest circle appears with blue border and with blue countdown.

You may quit the application at any moment. Launching it back will restart countdowns to your last saved preferences, or with no selection at all if the related box was unchecked the last time you closed the application.


The application has been developed for and tested on 4 virtual devices and on two physical devices with different screen resolutions and sizes. The targeted Android version runs on nearly 99% of today devices. So, you should not experience any issue on your device, unless you are particularly unlucky.

That's it!


---
## Developer documentation

Current version of code is v0.1. It is released as release v0.1.0 

Release 0.1.0 code is a very first version of the application code. It is minimalist, not well architectured, and is released as is mainly because:
1. code executes with no detected bug
2. the application is simple enough
3. it allows the playing of the app for tests and evaluations performed by users

We expect this will help improving quickly and greatly the application functionalities.

A Release 0.2 will add at least one feature and will give us an opportunity of refactoring the code in a satisfaying manner.

The important thing to notice is that current code has been created by an AI Agent supervised by the conceptor of the app. Not knowing anything about Android OS specs as well as kotlin programming, learning and understanding the whole would have needed weeks if not few months of reading and testing.  
Installing Android Studio, abandoning the proposed default tutorials (definitively of no use) and connecting to Gemini to provide its AI Agent with short and progressive prompts led to:
* a very quick development of correctly running code;
* an easy way to refine specs and reqs by our side;
* a quick overview of Android concepts and kotlin programming;
* a surprising capacity of the AI agent to greatly understand the prompts in natural language - really suprising;
* a much bigger surprising capacity of the AI agent to understand what was asked to it and what would have been next steps without having told them to it - far more much surprising;
* a provided architecture for code which is far not optimal about future maintenance, for sure because of the step-by-step way we decided to ask for the code.
* a scarry feeling about the near future for coders - but not for long;

Well, until the AI agent began to provide buggy code - finally reassuring about the near future of coders.

The behavior of the AI Agent has been a little bit deceptive in that it was proposing buggy code, was informed of bugs with their related error messages (at compile time or after testing the running code), and then was totally able to propose correct fixes on its own bugs (sometimes after some rounds of tests and errors). So, the related AI is able to fix its own errors but still first propose them without preliminar verifications!

Finally, the explanations provided by the AI Agent were intelligible. They have been of great help for progressing and for learning. Interesting to see also that they were complete - as long as we can get it - but not always provided with the latest avalaible informations (libraries versions for instance, or latest version of Android Studio, i.e. Narwhal by Aug. 2025).

So, Release v0.1 code is small code that works well but which is rotten. Discussing via prompts with Gemini AI Agent helped providing a fully running app in an 8 days long journey while not knowing anything about the environment, the platform, the IDE and the programming language (while being a computer scientist which for sure helped a little bit).

Much conclusive:
* interacting with a very specialized AI is of great help;
* it considerably speeds-up a first version code development;
* it considerably speeds-up as well as the learning of the environment;
* meanwhile, it seems that it can't yet be successfully used by a novice coder or a no-coder-at-all to get a full running application which embeds even minimalist complexity.


---
## Want to contribute?
Of course you can contribute!

This project is Android and Kotlin based. Its creator did not know anything about this operating system and this programming language. He extensively used the help of Google Gemini AI agent in Android Studio (Narwhal version, the latest one of Android Studio by Aug. 2025) to help start this project.

Amazingly, Gemini quickly understood what was going on, quickly and correctly foreseen what would be further developments and proposed basic stuff correctly functionning, properly related to what was expected. This is quite scarry for a professional coder whose skills have been offically certified in many programming languages. Meanwhile, some persistent errors or bugs neeeded human interaction to fix them.

Devs are now in progress and any help is welcome. If you are familiar with Github and know how to code in Kotlin for Android, for sure you can help! Should you know iOS also, you could take the lead for iOS devs also!

### Programming Environment Guidelines
The preferred IDE for devs is Android Studio.  
Related stuff should be available in releases branches (see further subsection Branches rules) to help you get a full development environment already set.

### Fork this repository
To begin contributing, first fork this repository at any place of your choice in your own environment (PC, cloud, ...).

You should then get a full ready-to-code environment in Android Studio.

### Respect rules and recommendations
Smartly respect this project [code of conduct](CODE_OF_CONDUCT.md) and stricly respect the coding and contributing recommendations (see next subsections Branch rules and Coding recommendations)

### Branches rules
These are rules. They are not recommendations. You cannot bypass rules ;-)

Branch `main` is reserved for the direct access to the latest release.  
The administrator of this repository is the only person allowed to merge any release to branch `main`.  
No release can be created from elsewhere than branch `main`.  
The administrator of this repository is the only person allowed to create releases.

Releases are developed in dedicated branches named according to the template `release-MM-mm` where MM is the major number of the release and mm is the minor number of the release. Example: `release-0-1`, which is the very first release that is being worked on.

Any contributor can create sub-branches from any release branch. Her devs will be done in the sub-branch she will have created. This stands for the related commits as well.  
Any contributor can create Pull Requests from her created branches to its parent branch `release-MM-mm`.

It might be that in a near future not all contributors will be allowed to merge their branches to the parent release one.

### Coding recommendations
These are recommendations. You should try to not circumvent them too much.

**First, conform to Kotlin coding recommendations**.

As an example, use the coding rules that implicitly appear in the actual code. Up to end of Aug. 2025 this code has been provided by a Gemini AI agent (while sometimes manually debugged by a human) and is supposed to conform with these recommendations. So, it is a very good starting point.

**Write easy-to-read code:**
* use self explaining names for classes, methods, instances, functions, variables, constants and the like;
* use empty lines, for instance to separate any grouped items from the next grouped ones;
* do not hesitate to NOT too much optimize code, CPU optimization is not a goal for this project while easy-to-read-code is.

**About the User Interface:**
* the application will only be used in Portrait mode. This is a mode system that is deprecated now with Android, but this is a design choice: whatever the position of the device things will then always be displayed at the same places relative to the others;

* always ensure that display will be ok whatever the resolution of the display device:  
Targeted standard is 1080 x 2400 px (e.g. Medium Phone API 36 from Android Studio - 412 x 915 dp);  
Smaller targeted one is 720 x 1080 px (e.g. Small Phone, API 31 from Android Studio - 360 x 480 dp);  
Bigger targeted one is 1440 x 3120 px (e.g. Pixel 6 Pro from Android Studio - 418 x 892 dp);

* always favor circles for clickable graphical items: this is the offical shape of almost all World Archery targets;

* do not aim at modifying the actually defined colors for items: these are the official colors specified by World Archery for its targets, i.e.  
    Yellow: 0xFFFFE552 (Pantone 107U)  
    Red: 0xFFF65058 (Pantone 032U)  
    Blue: OxFF00B4E4 (Pantone 306U);

* You may also use  
    Black: 0xFF000000, preferably for background (energy saving purpose)  
    White: 0xFFFFFFFF, preferably for text, and exceptionnaly  
    Teal: 0xFF387F97 (Pantone 4185C) which will be used as experimental for targets on a specific World Archery championship;

* Sole exception about those colors: background. It is currently set as dark gray, and even darker gray for grouping timers and countdowns just to visually separate them from their related controls.

* Maintain the UI aspect as simple as possible. This may seem frustrating for UI/UX designers but this application is very simple and offers very simple service.  
Just use a few different colors, sufficiently different form each other.  
Just use a few different graphical item types.  
Don't show a same information multiple times (e.g. a countdown with numbers contained in a surrounding circle should not display in the meantime any progression bar or overridden progression circle).

**Finally, document your code using KDoc comments**.

This is very easy - see [https://kotlinlang.org/docs/kotlin-doc.html](https://kotlinlang.org/docs/kotlin-doc.html) - and it will greatly help automate the documentation of the code.

---
That's it. Enjoy!

---