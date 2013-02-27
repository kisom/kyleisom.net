---
layout: post
title: "So, You Want To Unit Test in Xcode (Part 2)"
date: 2012-03-16 12:13
comments: true
categories: [xcode, objective-c, development, testing]
---

In the [last post](/blog/2012/03/15/so-you-want-to-unit-test-in-xcode), I 
talked about getting unit testing set up in Xcode, why you should write
unit tests, and what kinds of things you should unit test. Now, I'd like
to talk a bit more about *how* to write unit tests. If you come from a
background doing unit testing, as I did, it's very straightforward. If not,
I'll spend a little time explaining things a bit more.

When you generate a test case, you get a test class (which is a subclass of
`SenTestCase`). Just like any other class, you can declare members and methods,
which are used to perform helper tasks and carry state.

A very basic codebase only requires test methods. 
[OCUnit](https://developer.apple.com/library/mac/#documentation/DeveloperTools/Conceptual/UnitTesting/00-About_Unit_Testing/about.html)
will load any method prefixed by `test`. These methods must return `void` and
take no parameters. After setting up the test in the method, you can use the
`ST...` macros to actually test the results. Here's a contrived example:

```objc
-(void)testAdder
{
    int result = add(1, 1); // should return 2
    STAssertTrue(result == 2, @"1+1 should be 2!");
}
```

There are a number of test macros, which are listed in 
[Appendix B](https://developer.apple.com/library/mac/#documentation/DeveloperTools/Conceptual/UnitTesting/AB-Unit-Test_Result_Macro_Reference/result_macro_reference.html#//apple_ref/doc/uid/TP40002143-CH9-SW1)
of the [Xcode Unit Testing Guide](https://developer.apple.com/library/mac/#documentation/DeveloperTools/Conceptual/UnitTesting/00-About_Unit_Testing/about.html).

A real example taken from [FlexArgs](https://github.com/kisom/flexargs):

```objc
- (void)test_init_with_NSArray
{
    NSArray *testArgs = [NSArray arrayWithObjects:
                         @"foo=bar",
                         @"baz=1",
                         @"quux=-2.5",
                         @"spam=footastic",
                         @"eggs=false", nil];
    NSDictionary *expected = [NSDictionary dictionaryWithObjectsAndKeys:
                              @"bar", @"foo",
                              [NSNumber numberWithLongLong:1], @"baz",
                              [NSNumber numberWithDouble:-2.5], @"quux",
                              @"footastic", @"spam",
                              [NSNumber numberWithBool:false], @"eggs", nil];
    DNFlexArgs *flexArgs = [[DNFlexArgs alloc] initParserWithNSArray:testArgs];
    NSDictionary *parsed = [flexArgs retrieveArgs];
    
    STAssertTrue([parsed isEqualToDictionary:expected], 
                 @"initParserWithNSArray failed to return the expected NSDictionary!");
}
```

This test is set up to verify that the dictionary returned by `DNFlexArgs` is
what we'd expect it to be. `[flexargs initParserWithNSArray:]` expects an 
`NSArray` of NSStrings to be passed to it, I've initialised an `NSArray` of
`NSString`s. Because `DNFlexArgs` returns an `NSDictionary`, I've set up one
with the expected results. Then I setup the instance, pass it the initial 
test arguments, and retrieve the results. The last step is to assert that the
returned `NSDictionary` matches the expected one. I've made sure to hit one of
each kind of possible argument that can be passed in. 

In the last post, I listed some questions to help develop tests:

> * Right now, I'm only testing what I expect possible input *could* be. What 
> if someone passes in just `arg` or `arg=` - do I know that my code will handle
> that gracefully?
> * What happens if I pass in an overflowed numeric value? I've tried to prepare
> for this by using `long long` values, but how do I know my code is doing the 
> right thing?
> * What if I craft a special string that does something shifty, like embedding 
> a null byte? What happens then? What if the string has more than one `=` in it?
> * What happens if I [feed the parser random data](http://pages.cs.wisc.edu/~bart/fuzz/)?

If you look in the source, you'll see I've covered all but the last. That's 
because I haven't yet found a good fuzzing library for Objective-C. However,
the new tests allowed me to make a few improvements to and verifications of 
FlexArgs:

* I've verified that having an argument with no `=value` yields a boolean
value (i.e. `arg` results in an `arg = 1;`)
* I've verified that having an argument with an empty value (i.e. `arg=`)
yields an empty string value (i.e. `arg = @"";`)
* I was able to add support for multiple `=` in the class. Previously, only
the part of the value after the first `=` and before any other `=`'s was
captured. For example, passing `foo=bar=baz` resulted in `foo = @"bar";`
and `operator===` resulted in `operator = @"";`. Now, I get `foo = @"bar=baz";`
and `operator = @"==";`.
* I was able to verify that passing a null byte in the middle of the string
just cut the string off at the null byte instead of causing problems.

You can see the new code coverage output (as detailed in the 
[last post](/blog/2012/03/15/so-you-want-to-unit-test-in-xcode/)) 
[here](/downloads/FlexArgsCoverage2/). 

## More advanced test cases with `setUp`, `tearDown`, and class members
OCUnit gives us more control over our test cases. Just like any other class,
we can include our own members in the class by putting their definitions
and any `@property` declarations in the interface. For example, if we're
testing network code, we might want to create a socket. 

If your members need to be set up for every test, or if certain preparation
needs to be done before each test (like clearing out a temporary directory),
you can reduce code duplication by putting the code in the `setUp` and
`tearDown` methods. The `setUp` method is called before each test method, 
and the `tearDown` method is called after each. If you're calling the same
code before each test, you might consider moving them. If most of your tests
are calling the same code and a few aren't, consider creating a new test case
for the ones that don't, and moving the duplicate code into these methods and
members.

## guard-xcode
In the [last post](/blog/2012-03-15/so-you-want-to-unit-test-in-xcode/), I 
mentioned [guard](https://github.com/guard/guard/). What is guard?

> Guard is a command line tool to easily handle events on file system modifications.

We can use this to trigger a build every time a source file is changed. 
Unfortunately, I couldn't find any good Guards (the term for specific tasks to
be done on a changed-file event) to handle running configurable builds. To
address this, I wrote a Guard called `guard-xcode`, which kicks off an Xcode
based on the options you configure it with. The source is, of course,
[on Github](https://github.com/kisom/guard-xcode) and it's on 
[RubyGems.org](https://rubygems.org/gems/guard-xcode), so it can be installed
via `gem` or `bundle install`. The [README](https://github.com/kisom/guard-xcode/blob/master/README.md)
explains how to get started.

For FlexArgs, the setup is fairly straightforward. I already have
[growl-notify](http://growl.info/downloads#generaldownloads) installed, so all
I have to do is create my Gemfile:

```ruby
source :rubygems

gem "guard-xcode"
gem "guard"
gem "growl"
gem "rb-readline"   # better interface for MRI
```

I'm using [rvm](http://rvm.beginrescueend.com), so I've already got the 
`bundle` gem installed. You can install it with `gem install bundle` if you
need to. The next step is to run `bundle install`, and then `guard init xcode`.
Of course, the Guardfile doesn't know the name of your target, so you'll need
to open the Guardfile and edit it. Mine looks like this:

```ruby
# template Xcode guard

notification :growl

guard :xcode, :target => 'FlexArgs', :quiet => true, :clean => true do
  watch(/^.+\.[hmc]$/)
end
```

Now all you need to do is run `guard` in your project root. Once files change,
`guard` will kick off a build. I've set `:quiet => true`, so I only get Growl
notifications if the build has warnings or errors.

## Some final ideas for a test-based development cycle
There's a few options you can set in your project build settings that I've 
found quite useful:

0. Setting **Test After Build** to **Yes** runs tests anytime a build is done.
0. Setting **Treat Warnings as Errors** to **Yes** adds more emphasis to 
writing good code.
0. Adding the Test product to the main build makes testing easier as well, and
means you can set your build target to the main target, and still test.

## Conclusion
This concludes the two-part series on Unit Testing in Xcode. I've tried to
document what I learned trying to get testing set up, and hopefully other
people will find it helpful as well.

## References

* [XCode Unit Testing Guide](https://developer.apple.com/library/mac/#documentation/DeveloperTools/Conceptual/UnitTesting/00-About_Unit_Testing/about.html)
* [Code Coverage with Xcode 4.2](http://www.infinite-loop.dk/blog/2011/12/code-coverage-with-xcode-4-2/)
* [FlexArgs tag for this project](https://github.com/kisom/flexargs/zipball/blog-post2)
* guard-xcode on [Github](https://github.com/kisom/guard-xcode) and [RubyGems](http://rubygems.org/gems/guard-xcode)
* [Guard on Github](https://github.com/guard/guard)
* [growl-notify](http://growl.info/downloads#generaldownloads)
* [rvm](http://rvm.beginrescueend.com)
