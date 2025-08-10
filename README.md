# ArcheryTrainingTimer
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
<img src="./picts/001.jpg" alt="launched app screen" width="300"/>

The first thing to doit to select the options for the training session:
* duration of each repetition  
you will be helped to not do successive exercises at a faster or slower pace, beeps and display will help you
* number of repetitions in each series  
a graphical display will help you know how much of current series you have already completed. A double beep will inform you that you have completed a series.  
Notice:  


---
## Developer documentation

...to come with Release 0.1 - not yet released.  
Branch `release-0-1` contains current devs. Have a look to it if you are curious.


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