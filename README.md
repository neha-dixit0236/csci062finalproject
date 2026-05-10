# CS62 Final Project
Better Wrapped is a more personalized and context-aware experience that not only summarizes music listening history but why and when they listened. It is especially tailored to students by looking at semester and year schedules. Write-up with interview notes and project motivations are [here](https://docs.google.com/document/d/11LSb0u0Y-2pWvhK-tQDKIbNrH4US5A39U8TY9_U_cIk/edit?tab=t.0).

## Project Intro: What even is Better Wrapped?
At the end of every year, Spotify releases “Spotify Wrapped,” a slideshow that summarizes users’ listening habits over the past year. It compiles data on each users’ favorite artists, songs, and total minutes listened, and presents the information in a way that makes it easy for users to share their music summaries with each other. However, this music summary is static. It does not provide any insight into the ways a user’s listening habits change throughout time periods. We believe that music listening data is closely connected to one’s daily routines, emotional states, and life events. Thus, we built Better Wrapped, a more personalized and context-aware experience that not only summarizes listening history but also allows users to understand the differences between their music tastes during various academic time windows.

Specifically, Better Wrapped introduces three key features. First, it maps listening trends to important academic events: weekends vs weekdays, midterms vs academic breaks vs normal days in a semester, or differences in fall and spring semester and summer break. Second, our program detects outliers in students’ listening habits that don’t fit with their normal listening taste during the time window. Third, based on a users’ top genre in a time period, Better Wrapped provides song recommendations that the user might also enjoy listening to. With this project, we hope that Better Wrapped will show students how their academic lives contextualize their listening habits.



### Data Structures
We will be implementing a list of key-value pairs, with the timestamp of each song being the key. Additionally, we will create a SongInfo object to contain information about each song, and the SongInfo object associated with each timestamp will be the value in the key-value pair.
We will also be mapping from bucket (e.g. midterm, break, spring semester, etc) to a list of songs played in that bucket, for Feature 2.

### The Data Set
We use data from [last.fm](http://last.fm). [This dataset](https://www.kaggle.com/datasets/basharsalman/lastfm) has timestamps as well as the artist, song name, and genre of each song. We will use [this dataset](https://www.kaggle.com/datasets/jacopoferretti/chinook-music-database?select=archive), which includes lots of different songs, for Feature 3.

### Feature 1: Listening Trend Analysis
Look at how users’ listening behavior changes across different academic time periods. It will do so by analyzing song genres, artists, and top songs over a specified time window.

### Feature 2: Detecting Outliers
Find days where a student’s listening behavior is different from their normal listening habits (by the genre they listen to the most) in the established time period and output the “outlier” days and genre differences to the user.

### Feature 3: Focused Recommendations
Give the user song recommendations based on the student’s listening habits (using the songs and genres they’ve listened to) during the time window chosen. We will do this by recommending songs that are the same genre as the student’s most popular genre.