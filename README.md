# CS62 Final Project
Better Wrapped is a more personalized and context-aware experience that not only summarizes music listening history but why and when they listened. It is especially tailored to students by looking at semester and year schedules. Write-up with interview notes and project motivations are [here](https://docs.google.com/document/d/11LSb0u0Y-2pWvhK-tQDKIbNrH4US5A39U8TY9_U_cIk/edit?tab=t.0).

## Data Structures
We will be implementing a list of key-value pairs, with the timestamp of each song being the key. Additionally, we will create a SongInfo object to contain information about each song, and the SongInfo object associated with each timestamp will be the value in the key-value pair.
We will also be mapping from bucket (e.g. midterm, break, spring semester, etc) to the songs played in that bucket, for Feature 2.

## The Data Set
We use data from [last.fm](http://last.fm). [This dataset](https://www.kaggle.com/datasets/basharsalman/lastfm) has timestamps as well as the artist, song name, and genre of each song. We will use [this dataset](https://www.kaggle.com/datasets/jacopoferretti/chinook-music-database?select=archive), which includes lots of different songs, for Feature 3.

## Feature 1: Listening Trend Analysis
Look at how users’ listening behavior changes across different academic time periods. It will do so by analyzing song genres, artists, and top songs over a specified time window. Uses `BetterWrapped2`.

## Feature 2: Detecting Outliers
Find days where a student’s listening behavior is different from their normal listening habits in the established time period and output the “outlier” days and genre differences to the user.

## Feature 3: Focused Recommendations
Give the user song recommendations based on the student’s listening habits (using the songs and genres they’ve listened to) during the time window chosen. We will do this by recommending songs that are the same genre as the student’s most popular genre.