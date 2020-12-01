# Workoutio

An Android workout tracker

This app tracks workout routines.
It helps user to easily enter his workout exercises, see their change over time and modify routines.

## Table of contents
* [Technologies](#Technologies)
* [Features](#Features)
* [Screenshots and usage](#Screenshots_and_usage)
* [Database overview](#Database_overview)
* [What I've learned](#What_I've_learned)


## Technologies

Java

Android

XML 1.0

SQLite

## Features

Editing exercises: selecting muscles, allocating days and exercise preferences

Adding previously done exercises later

Automatic exercise amount and repetition hints

Dynamic exercise history, updating after new exercises are added

Dynamic exercise length calculation

## Screenshots and usage

*Main screen*
Main screen shows muscles that are in use this day. After clicking this screen, exercises are listed along with their quantity and repetitions. Those are based on previous workouts.

*History*
History shows past exercises. They are grouped based on the day and can be expanded.

*AddMenu*
Menu for adding exercises. Date is added via calendar and hour can be picked with a clock.

*ExerciseMenu*
Exercises are divided into 3 categories:
Those assigned to a day, 
Exercised which were edited or performed, 
Stock exercises

Every exercise can be edited

## Database overview



## What I've learned
This is my biggest project yet, so I've learned a ton

Details of working with a database

Starting Activities, sending and retrieving data from them

Themes, ActivityFragments, different widgets, dynamic view inflation

Diverse RecyclerViewAdapters, how recycling works, how it affects methods, different rows, methods of updating data

Different Listeners, MutableLiveData

Debugging, measuring how long it takes for a method to execute
