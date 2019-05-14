# Face finder

> I created android application that find faces in your local photos and help to organaze them.

## What is it?

This application use OpenCV library for finding faces by Haar features. Also there is handwritten photo gallery that uses AsyncTask to allow to list photos smoothly. And there is possibility to view big photo in details by zooming in and out details using sophisticated mechamism with low memory.

# Table of Contests

- [What is it?](#what-is-it)
- [Settings](#settings)
- [How it works?](#how-it-works)

## Settings

To build this application the next is require:

* android sdk 21+
* android-ndk r10e
* OpenCV android sdk 2.4.11.0
* gradle 2.8+

## How it works

There used OpenCV library for finding faces. Library is called by JNI library.
