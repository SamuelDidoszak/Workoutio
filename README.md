# Workoutio

An Android workout tracker

This app records and tracks workout routines.

It allows users to easily enter their exercises, plan daily routines while targeting certain muscle groups and record exercises in real time.

## Table of contents
* [Technologies](#Technologies)
* [Features](#Features)
* [Screenshots and usage](#Screenshots_and_usage)
* [What I've learned](#What_I've_learned)


## Technologies

Java

Android

Gradle

XML 1.0

SQLite

## Features

Adding and editing exercises: selecting muscles, exercise preferences, and assigning exercise to certain day of the week

Recording exercises in real time

Flexible workout routines

Adding previously done exercises later

Dynamic recommended exercise amount and repetition calculations

Dynamic exercise history, updating after new exercises are added

## Screenshots and usage

<img src="https://user-images.githubusercontent.com/70522994/168899026-5e1e2999-25d1-4fe5-bc23-e7934b908c87.jpg" width="17%" align="right"></img>

### *Main screen*

Main screen shows muscles that will be in use this day. After swiping this screen, the workout starts with exercises from the day group and recommended repetition amounts calculated from the amount done in previous days.

*History*

History shows past exercises. They are grouped based on the day and can be expanded.

<img src="https://user-images.githubusercontent.com/70522994/168905013-d25462e4-20a0-4edc-8639-5e1ec2cc6ea8.gif" width="17%" align="left"></img>


### *AddMenu*
Menu for adding past exercises if they werent recorded with the app. Date is added via calendar and hour can be picked with a clock.

This view also lets the user access exercise and group screens.

### *ExerciseMenu*

<p width="100%" align="right">
<img src="https://user-images.githubusercontent.com/70522994/168907749-4509713f-890b-4ab7-8f58-0e7102d75277.gif" width="17%" align="right"></img>
</p>

Exercises are divided into 3 categories:
  * Those assigned to a day or a group
  * Custom exercises, or those edited 
  * Stock exercises

<br/>
<br/>

<img src="https://user-images.githubusercontent.com/70522994/168911833-a5d663da-67ce-46e4-8ec3-98cc4033e681.gif" width="17%" align="left"></img>

Every exercise can be modified. Doing that makes them custom and every instance of this exercise finished in the past changes to this one. User can also add their own exercises.

Everything is synchronous and there is no need to refresh anything, because changes done in one activity have an immediate impact on every other activity. This is achieved by passing necessary data to interested activities or fragments and parsing them accordingly.

<p align="right">
<img src="https://user-images.githubusercontent.com/70522994/168912393-3b116d4e-e661-4797-aaec-917d63848c87.gif" width="17%" align="right" display="inline"></img>
</p>

<img src="https://user-images.githubusercontent.com/70522994/168915062-ea9433e3-e5b6-466b-a8c6-c4a4ba958e33.gif" width="17%" align="right" display="inline"></img>

### *Day/Groups*

Exercises are assigned to particular days of the week, and when the day comes they create 

Users can also create groups of exercises, which can be easily added during workout.

### *Workout screen*

When workout starts, all of the exercises assigned to this day of the week are added to the list. User can follow the order of exercises, or pich whichever he wants. All of the remaining exercises stay in the previous order.

Every exercise has its own initial prompted amount of repetitions, which are based on the amount done in previous weeks. If user has finished exercise after a different number of repetitions, he can change the amount.

<img src="https://user-images.githubusercontent.com/70522994/168916918-bc92a289-24e9-41f6-8caa-6c522ffd7af4.gif" width="17%" align="left"></img>

The chronometer has its own gesture actions. Clicking it starts or finishes the exercise, swiping it vertically pauses the exercise and swiping horizontally shows the overview screen containing the list of all exercises done in the workout. They can be edited there.

<img src="https://user-images.githubusercontent.com/70522994/168917036-11c5da0a-b91e-4769-bed9-c2c1cfa8ef71.gif" width="17%" align="right" display="inline"></img>

During the workout, user can also do other exercises, and add to the list either one exercise or a group of exercises.

When the workout finishes, the main screen no longer contains the muscle list for the day, but the list of exercises done this day.

### *Perserverance*

Every activity is failproof and does not crash no matter what user does. Every change is saved and available on the next startup or resuming of the app

All exercises are saved dynamically and user can continue them even if he closes the app mid-workout.

<br/>

## What I've learned

This is my biggest project yet, so I've learned a ton:

* Programming bigger applications and what challenges does it create
* Details of working with a database
* Various optimizations
* Modifying libraries
* Various OOP aspects in practice
* Debugging, measuring how long it takes for a method to execute and optimization
* Dynamic communication between classes
* Starting Activities, sending and retrieving data from them
* Themes, ActivityFragments, different widgets, dynamic view inflation
* Diverse RecyclerViewAdapters, how recycling works, how it affects methods, different rows, methods of updating data
* Different Listeners, MutableLiveData
