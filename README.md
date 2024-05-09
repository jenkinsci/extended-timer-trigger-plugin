# Extended Timer Trigger

## Introduction

This plugin adds another timer based trigger to jobs. It has an extended syntax that allows for better scheduling of weekly or monthly triggers. 

## Getting started

In your job you will find a new trigger `Build periodically with extended Syntax`. 

![See](/docs/img.png)<br/>
![Configured](/docs/configured.png)<br/>

You can add one or more cron like entries with the 5 fields
<pre>Minute Hour Day-of-Month Month Day-of-Week</pre>


You can use the same syntax as for the Jenkins provided `Build periodically` but also an extended syntax to e.g. run a job on the last 
day of a month, or only on the second Tuesday, or on the last weekday.

In the extended syntax the special character `H` from Jenkins (which stands for a random value within ranges) is not supported.

### Fields 

| Field        | Allowed Values | Allowed Special Characters | Comment                               |
|--------------|----------------|----------------------------|---------------------------------------|
| Minutes      | 0-59           | , - * /                    |                                       |
| Hours        | 0-59           | , - * /                    |                                       |
| Day-of-Month | 1-31           | , - * / ? L W              |                                       | 
| Month        | 1-12, JAN-DEC  | , - * /                    | case doesn't matter                   | 
| Day-of-Week  | 0-7, SUN-SAT   | , - * / ? L #              | 0 or 7 is sunday, case doesn't matter | 


### Special Characters

| Char | Description                                                                                                                                                                                                                                                                                                                                                                                                                            |
|------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| *    | Matches all values of the field. E.g. `*` in the minute field means every minute.                                                                                                                                                                                                                                                                                                                                                      |
| ?    | Stands for 'no specific value' and is allowed for the Day-of-Month and Day-of-Week fields. It is used instead of the asterisk (*) for leaving either Day-of-Month or Day-of-Week blank.                                                                                                                                                                                                                                                |
| -    | Used to define ranges. E.g., <em>10-12</em> in the hour field means the hours of 10, 11, and 12.                                                                                                                                                                                                                                                                                                                                       |
| ,    | Used to separate items in a list. E.g., MON,WED,FRI in the Day-of-Week field means the days Monday, Wednesday, and Friday.                                                                                                                                                                                                                                                                                                             |
| /    | Used to indicate increments. E.g. <em>0/20</em> in the minutes field means the seconds 0, 20 and 40. <em>1/4</em> in the Day-of-Month field means every 4 days starting on the first day of the month.                                                                                                                                                                                                                                 |
| L    | Short for "last" and can be used in the Day-of-Month and Day-of-Week fields. The <em>L</em> character has a different meaning in the two fields. In the Day-of-Month field, it means the last day of the month. In the Day-of-Week field, it means 6 or SAT. If used in the Day-of-Week field after a number, it means the last xxx day of the month. E.g., <em>6L</em> in the Day-of-Week field means the last Saturday of the month. |
| W    | Stands for "weekday" and is only allowed for the Day-of-Month field. The <em>W</em> character is used to specify the weekday nearest to the given day. E.g., <em>10W</em> in the Day-of-Month field means the nearest weekday to the 10th of the month. If the 10th is a Saturday, the job will run on Friday the 10th. <em>W</em> can be combined with <em>L</em> to <em>LW</em> and means last weekday of the month.                 |
| #    |  Can only be used in the Day-of-Week field. Used to specify constructs. E.g., <em>5#3</em> means the third Friday of the month.                                                                                                                                                                                                                                                                                                                                                                                                                                      | 


Lines starting with `#` are comments. You can also add comments with `//` after a cron entry
## Samples
```
0 5 L * *  // run at 5:00 on the last day of the month, run in the controller timezone
0 5 LW * *  // run at 5:00 on the last workday (Mon-Fri) of the month

TZ=Europe/Berlin
0 5 * * THUL  // run at 5:00 on the last Thursday of the month in timezone Europe/Berlin
0 5 ? * 2#3  // run at 5:00 on the third Tuesday of every month

TZ=America/Toronto
0 5 * FEB WED#2 // run at 5:00 on the second Wednesday in February, runs in timezone America/Toronto

TZ=
H 12 * * 5  // Jenkins syntax, runs in the controller timezone
H 5 * * *   // run once between 5:00 and 5:59 every day, this is the standard Jenkins syntax, this can't be combined with
            // L, W or #
```

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

