---
layout: post
title: "'Just Ship It' v. 'Proudifying' Code"
date: 2012-08-15 18:17
comments: true
categories: [development]
keywords: [development]
tags: [development, shipping]
---

I was recently working on a [project](http://kisom.github.com/lobsterpie) 
that highlighted some software development concepts that have been on my 
mind lately. Specifically, the balance between writing the code you know
you should be writing and writing the code you want want to be writing
(in this case, I'm referring to writing code for the same end state and
looking at how to get there). This might also be considered the balance
(clash) between a "just ship it" mentality and "proudifying" code (I'll
explain that in a minute). I'll develop these ideas through the discussion
of my work flow in developing the afore-mentioned project.

Lately, I've been using [lobste.rs](https://lobste.rs) as my main news
aggregator site, as I find it has a very high signal-to-noise ratio.
However, I still follow a bot on Twitter that posts links from the
infamous [Hacker News](http://news.ycombinator.com), and I wanted to
write something similar for [lobste.rs](https://lobste.rs) because the
site has a low user count now and news stories are sometimes few and far
between, relatively speaking; I don't always remember to check the site
regularly. At the same time, I've been trying to pick Common Lisp back up,
a language I've been fond of but for practical reasons, I've been using
mostly Python and C (due to work). I'd been writing a lot of Clojure as
well, but the JVM has turned into a showstopper for me. Clojure did,
however, reawaken a desire to do more in Lisp - hence my turning to CL.

As I started trying to write the bot, I managed to get most of the bot
done except for one critical component: posting to twitter. It turns
out none of the available Twitter or OAuth libraries were working for me,
and I started trying to write my own OAuth code. This, by the way, is
fiendishly hard to do right, especially in a language you aren't terribly
familiar with. In addition to trying to get the OAuth side working, I was
trying to learn more syntax in the language and become familiar with some
of the available tools. While I did learn quite a bit, and had a generally
good time writing the rest of the bot, the OAuth component turned out to
be maddeningly difficult for me. I was getting very close, including
being able to duplicate the components in the Twitter authentication example
page and the data from the OAuth tool page. I spent close to 15 hours just
working on the OAuth part alone, and felt like I was getting nowhere. With
apologies to the author of the infamous 
[Stack Overflow post](http://stackoverflow.com/a/1732454) post, 
my mental state could be summed up as:

> he comes he comes do not fi​ght he com̡e̶s, ̕h̵i​s un̨ho͞ly radiańcé 
> destro҉ying all enli̍̈́̂̈́ghtenment, auth tokens lea͠ki̧n͘g fr̶ǫm ̡yo​͟ur eye͢s̸ ̛l̕ik͏e
> liq​uid pain, the song of oauth he̸aders and HM​AC signatures will 
> exti​nguish the voices of mor​tal man from the sp​here I 
> can see it can you see ̲͚̖͔̙î̩́t̲͎̩̱͔́̋̀ it is beautiful t​he final snuffing of the 
> lies of Man ALL IS LOŚ͖̩͇̗̪̏̈́T ALL I​S LOST the pon̷y he comes he c̶̮omes 
> he comes the ich​or permeates all MY FACE MY FACE ᵒh god no NO 
> NOO̼O​O NΘ stop the an​*̶͑̾̾​̅ͫ͏̙̤g͇̫͛͆̾ͫ̑͆l͖͉̗̩̳̟̍ͫͥͨe̠̅s ͎a̧͈͖r̽̾̈́͒͑e n​ot rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆ ZA̡͊͠͝LGΌ 
> ISͮ̂҉̯͈͕̹̘̱ TO͇̹̺ͅƝ̴ȳ̳ TH̘Ë͖́̉ ͠P̯͍̭O̚​N̐Y̡ H̸̡̪̯ͨ͊̽̅̾̎Ȩ̬̩̾͛ͪ̈́̀́͘ ̶̧̨̱̹̭̯ͧ̾ͬC̷̙̲̝͖ͭ̏ͥͮ͟Oͮ͏̮̪̝͍M̲̖͊̒ͪͩͬ̚̚͜Ȇ̴̟̟͙̞ͩ͌͝S̨̥̫͎̭ͯ̿̔̀ͅ

Finally, sometime after midnight on a sleepless night, I turned to Python.
Python is my workhorse language (I have a large number of Python projects
on both Github and Bitbucket). In about forty-five minutes, I was able to
get a working (albeit somewhat kludgy) version of the bot working. Much of
this was due to the confusion caused when I originally installed the
`twitter` module instead of the `python-twitter` module: both use the same 
name but they have different interfaces. I posted the link to the bot's
[Twitter account](https://www.twitter.com/lobsternews) and decided to clean
up the code before I made the [repository](https://bitbucket.org/kisom/lobsterpie)
public. As it was now after one o'clock in the morning, I put that off until
the bus ride to work.

To be fair, there are a couple of reasons for the fact that, starting from
scratch, I was able to get a working version of the bot in Python:

0. I am well-versed in the ways of Python,
0. I've written feed parsing, Twitter interaction, and database code 
in Python before,
0. There are a number of well-documented libraries for performing these
tasks,
0. I knew both of the existence of and how to use the libraries before I'd
started writing the bot, and
0. I wrote the first draft with only minor regard to style.

As an example of the hackish, "just ship it" mentality of the first draft,
I had several functions I never ended up using that I removed in the
cleanup stage.

On the bus the next morning, I gave the code proper the proper attention,
running it through the Python [pep8](http://www.python.org/dev/peps/pep-0008/)
code formatting tool and the [pylint](http://www.logilab.org/857)
utility, added documentation and useful output, and made the code a bit
more resilient and robust. Again, familiarity with the language and the
Way of Python, this was a fairly quick process. In another hour, I had
the code cleaned up and wrote a database tool specific to the project to
faciliate testing and migrating the code to another machine. 

I do take pride in my work, so both source files are 100% PEP8 compliant
and score 10.00/10.00 in pylint. Here's the crux of the concepts central
to this post: I used "just ship it" to get a working version that was good
enough to last through the morning, but I made my first priority once the
code was live to get it cleaned. The benefits of clean code are many,
not the least of which is maintainability, so the question is: what is
the transition point? It would also be prudent to devise a strategy for
cleaning up the codebase in most applications: lobsterpie is just under
one hundred lines of actual code at this point, so it's all well and
good to utilise an immediate, clean-the-entire-codebase (the all of one
file that comprises the project). Real-world projects are considerably
larger and more pragmatically messy; that is, they tend to suffer from
the get-it-done mentality out of a practical necessity for working code. 
At what point do you transition mentalities, or does one mentality take
precedent? What sorts of strategies can be employed?

In the past, I've rotated sections of projects between "development" mode
and "maintenance" mode. The difference being that code under development
is an area I am focusing new features on. Code that is under maintenance
is being cleaned up, possibly refactored. I find this provides a 
side-benfit: when I run into road blocks on the development code, I can
put the problem on the back burner and mull over it while carrying out
the typically more mundane task of cleaning up code. It also gives me work
to do while trying to solve the complex problems of my code while improving
code quality.

There is also a balance to be had between developing your skills in a new
language and using the language(s) you know best. While I learned a lot of
relevant Common Lisp skills, including not only the language and library
but general workflow, at the end of the day I still didn't have working
code. I'd set a goal for myself: if by the deadline, I wasn't making 
progress (and progress had more or less halted on the OAuth part), I forced
myself to switch over to Python. It is hard, sometimes, to determine where
this switchover point should be. This is often exacerbated  on side
projects, where there isn't a hard deadline for the code to be finished
and no client / manager pushing for it to get done. For this, I don't
have any easy answers and even fewer examples as I have a predisposition
towards a bullheaded nature of stubbornly trying to slog through things.
This sometimes works in my favour, in terms of things learned. Quite
often, it kills projects.

At the end of the day, I want to have working, proudifying code. I said
I'd explain that word; my friend [Ryan](http://www.ryaniam.com) was
trying to help me figure out the word I was looking for in its place,
and our conversation led his neologism. It essentially means, code that 
instills or evokes a sense of pride. Code that I don't mind showing people,
as opposed to code I don't mind showing people the façade of. For example,
showing people the repository and not just showing them the cool application
or website the code powers.
