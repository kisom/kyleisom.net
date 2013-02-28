---
layout: post
title: "So, You Want To Unit Test in Xcode"
date: 2012-03-15 19:19
comments: true
categories: [xcode, objective-c, development, testing]
tags: [xcode, unit testing, objective c, objective-c, iOS development, iOS unit testing, OCUnit, flexargs, guard-xcode, ruby-guard]
keywords: [xcode, unit testing, objective c, objective-c, iOS development, iOS unit testing, OCUnit, flexargs]
---

One of my personal preferences when testing 
[MVC](http://heim.ifi.uio.no/~trygver/themes/mvc/mvc-index.html) code is to 
test my model using a commandline test driver, so when 
[Samuel Goodwin](http://samuelgoodwin.tumblr.com)
and I were talking about testing code and he brought that up, we started
discussing ways to more effectively write those commandline drivers. Long story
short, we decided a useful strategy would be able to provide a library to parse
arguments like 'key=value' into a dictionary. Since we will be doing a lot of
iOS app work, and quite possibly desktop Cocoa work later on, we decided
writing a class to do this in Objective-C would be useful, and about 24 hours
later, [FlexArgs](https://github.com/kisom/flexargs/) was dumped onto the 
world. But that's not what I want to talk about here. Rather, I'd like to
discuss what I've learned about unit testing in Objective-C. As a developer
who does a lot of testing in C and Python already, I immediately made it a
priority to learn how to do this. In this post, I'll go over basic unit 
testing, doing code coverage, and writing better tests. I'm particularly
aiming this post at people who know how to code and test, but want to do it
more effectively in Objective-C or want to learn how to get started.

Through version 0.9.0, which was a functional version of the code missing a few
other pieces, I was using the autotools suite to manage the build. (Why? I know
autotools, and can set up the build environment quickly, whereas I don't use
Xcode enough to be terribly good at it.) Unfortunately, I couldn't find any 
good libraries to do it. One of the goals of the project was to learn Objective-C
better, so I buckled down and imported the project into Xcode. I subscribe to
the idea that it's a good idea to start writing tests before you write code,
so writing tests after the bulk of the code was written felt a little janky.
I digress. 

First things you'll need to do is add a new target (`File`->`New`->`Target`) 
and name it `(@"%@Tests", ProjectName)` (ex. MyClassTests), and save it.
Because this is a library I want to give other people, it's set up as a CLI
application with a minimal main that gives an example of how to use code, and
so we don't really need coverage for that. However, the test suite should be
exercising large portions of our code, so setting up code coverage for that is
a good way to make sure your tests are exercising your code fully. 

I'd like to point out here that while code coverage is great for making sure
all of your code is being touched, it's not a replacement for well-thought-out
tests. It's a useful tool in the tool box while you're developing code, and
great for profiling code to determine bottlenecks (but not
[prematurely](http://c2.com/cgi/wiki?PrematureOptimization)!), but you still
need to make sure you're testing your code fully. I try to take the time to 
consider edge cases, places where the code might act inappropriately, and try
to implement some fuzzing to throw unexpected things at the code. 

You can set up your main target to run tests as well. You'll need to 
edit the scheme:

![editing the scheme in xcode4](/img/unit_testing_xcode/xcode4_edit_scheme.png)

Once there, select `Test`, and click the `+` to add the test case bundle.

Now, we can set up code coverage. Under the project settings, select the test
target. Under the build settings, change the **Generate Test Coverage Files**
and **Instrument Program Flow** options to **Yes**. Now the fun part is adding
in `libprofile_rt.dylib`. I found it under 
`/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib`
in OS X 10.8. Under the `Build Phases` tab, you'll need to add this to the 
`Link Binary with Libraries` section. Once you start running tests, you'll start
getting coverage data.

I lied about there being only one fun part. The other fun part is getting that
data, which you will find in the `Projects` tab in the Organiser. There is a
line called `Derived Data` with an arrow next to it to open that folder in
Finder. You'll want to open that folder in your terminal emulator of choice.
For FlexArgs, I had to navigate to 
`DERIVED_DATA/Intermediates/FlexArgs.build/Debug/FlexArgsTest.build/Objects-normal/x86_64/`.
There, you should see some files named *.gcno, *.gcda, and so forth. 

At this point you'll want to install `lcov` which is, most fortuitously, in
HomeBrew. `lcov` gives us pretty HTML output of our code coverage (via the
included `genhtml` program). You'll also want to stick this small script in 
your path:

```bash
#!/bin/sh
# ccovhtml.sh
# usage:
#   ccovhtml.sh ClassName OutputDirectory
# example:
#   ccovhtml.sh FlexArgsTests ~/tmp/FlexArgsCoverage

lcov --base-directory . --directory . -c -o $1.info
genhtml -o $2 -t "$1 code coverage" --num-spaces 4 $1.info
```

The first argument is the name of output file to generate, and is also used
to generate the title for the HTML output. The second is the output directory
to store the files in. Once you've run your tests, run that script and check
out the results. For my code, it built [this page](/downloads/FlexArgsCoverage/).
Note that this is with two tests that don't yet test the full functionality
(specifically `+(id)parserWithNSArray:(NSArray *)inargv` and
`-(id)initParser:(char **)inargv nargs:(int)nargs`), so you can see that the
output highlights those lines. If you take a look at the [FlexArgs](https://github.com/kisom/FlexArgs)
source at [commit bec86374f3](https://github.com/kisom/flexargs/tree/bec86374f3876e8a8c44a17849a3f49c76245d1e)
or the helpful tag `blog-post` (you can grab a [zipfile snapshot](https://github.com/kisom/flexargs/zipball/blog-post))
you'll see I only have two tests, and they both touch basic functionality.

Intelligent and well-planned and executed tests offer many benefits:

0. Validate the program's logic
0. Drive development by letting you see what's not implemented yet
0. Perform [regression tests](https://en.wikipedia.org/wiki/Regression_testing) 
to let you know if changes broke your code, or if they've broken other parts 
of the codebase
0. Test edge cases to make sure your code doesn't do anything unexpected
0. Assist in identifying possible security issues.

So given this code, what kinds of tests could I write to improve the functionality?

* Right now, I'm only testing what I expect possible input *could* be. What 
if someone passes in just `arg` or `arg=` - do I know that my code will handle
that gracefully?
* What happens if I pass in an overflowed numeric value? I've tried to prepare
for this by using `long long` values, but how do I know my code is doing the 
right thing?
* What if I craft a special string that does something shifty, like embedding 
a null byte? What happens then? What if the string has more than one `=` in it?
* What happens if I [feed the parser random data](http://pages.cs.wisc.edu/~bart/fuzz/)?

As you can see, there's a lot to think about. While writing tests ahead of time
to verify basic functionality is a great idea (I wrote about
[README-Generated Test-Drive Development](http://www.kyleisom.net/blog/2011/07/04/rgtdd/)
previously), your tests need to go further to fully verify your code. Just by 
looking at the questions above and thinking about the tests, I can already 
see that my code needs some work to address some of those questions. I can 
write the tests to validate the changes I'll need to make.

I mentioned at the beginning that I like to test models using command line
test drivers. What this means is that I write a small command line target that
I can call from something like `make tests` or even `python testrunner.py` so
I can constantly run my tests. This way, I don't need to worry about the view
or the controllers to develop the model. This follows my ideal of developing
model first, and letting the controller and view follow from that. In Xcode,
we can do this from the commandline inside the project using
`xcodebuild -target FlexArgsTest -configuration Debug clean build`. 
Before you run this, set *Test After Build* to *Yes* in the Build Settings
to ensure the tests will run after building. (At some point, I'll write a 
[Guardfile](https://github.com/guard/guard) to automate testing.)

I hope you find this useful. Now, if you'll excuse me - I have more tests to write...

Update: I've written a [part 2](/blog/2012/03/16/so-you-want-to-unit-test-in-xcode-part-2/),
which covers a bit more and includes <del>black magic where the dark lord has destroyed
my soul and brought death, destruction, and chaos upon the world</del> a Ruby
gem I wrote to assist in testing. 

### References

* [XCode Unit Testing Guide](https://developer.apple.com/library/mac/#documentation/DeveloperTools/Conceptual/UnitTesting/00-About_Unit_Testing/about.html)
* [Code Coverage with Xcode 4.2](http://www.infinite-loop.dk/blog/2011/12/code-coverage-with-xcode-4-2/)
* [Regression Testing](http://drdobbs.com/tools/206105233)
* [Fuzzing](http://pages.cs.wisc.edu/~bart/fuzz/)
* [README-Generated Test-Driven Development](http://www.kyleisom.net/blog/2011/07/04/rgtdd/)
