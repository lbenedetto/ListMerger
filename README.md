# ListMerger
## Why does this exist
At my job, people need to be able to look up a VIN number and find out some relevant information about it. Unfortunately, all that data is tied up in a decades old database system called Lotus Approach. Worse still, records go back 15 years, and each year has its own database file. As if that wasn't enough, it takes 30 minutes for Lotus to open a database file. 

Luckily, Lotus has an "Export" function, and a CSV option. By merging each exported database file csv into one file (should only be a few MB) they will be able to quickly find information they need. Unfortunately, this comes nowhere close to replacing Lotus, but it will still save everyone loads of time.

## How to use
Run the program and click open
Select the files to merge
Click merge
Select the output destination file
![](http://i.imgur.com/8lKo3Cn.png)

The program will merge all the entries in the two CSV files using the first column as the super key and output them to a third file. Duplicates will be ignored. This is accomplished with a HashSet
