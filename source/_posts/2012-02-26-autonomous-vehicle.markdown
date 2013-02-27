---
layout: post
title: "Autonomous Vehicles"
date: 2012-02-26 17:33
comments: true
categories: [robotics, development, python]
---

I just finished up the first unit of [Udacity's](http://www.udacity.com) 
[CS373 (Programming a Robotic Car)](). 
It's been a lot of fun, and reminds me of why I love Python so
much. In this post, I'm just going to go over what the end result of
the first week has been.

[![Obligatory XKCD](http://imgs.xkcd.com/comics/python.png)](http://xkcd.com/353/)

(Obligatory xkcd...)

This unit was all Monte Carlo localisation, which generates
probability distributions as to where the robot is in the world. This,
of course, is all very simplified but it is still quite a fascinating
learning experience. 

The final homework question has us determining a probability
distribution (i.e. a matrix of probabilities guessing where the robot
is in the world) based on sensor readings and movements in a world of
red / green cells. For example, one of the first the worlds we're
given in the examples looks like this:

![simple world example](/img/cs373/unit1/simple_world.png)

You can see each cell in the world has a colour, and the robot's
worldview consists of a 2D matrix of probabilities. 

Given a list of sensor readings, i.e. `['green', 'red']` and a list
of corresponding motions in the form [y, x] such that `[1, 0]` is a
movement downwards and `[0, 1]` is a movement right, the robot should
be able to figure out where in the world it is. All the example code
in the class is done as simple functions operating in the `'__main__'`
namespace, but I used a Robot class to simulate everything. Testing
was painful, because I had to hand-type in most of the reference
probability distributions. Then,
[early Sunday morning](https://bitbucket.org/kisom/cs373/changeset/70b3d80194ee)
(actually late Saturday night), I discovered the
[`colorama`](http://pypi.python.org/pypi/colorama) module, which
faciliates pretty terminal outputs (i.e. colors, bolding, etc...) I
was able to write a method to print out the map (aka the `.showmap()`
method in the screenshots) that made testing where the robot was a lot
more fun.

The red/green world felt very contrived. I had a pretty good unit
testing framework set up, so I decided to test my code using a road
test: I built a world with three lanes that used black to represent
the asphalt, yellow to indicate lane markers, and white to indicate
the shoulder marker. I simulated having the robot start at the
shoulder, drive forward and then switch to the middle lane. I expected
the robot should believe itself to be somewhere in the middle
lane. 

The world looks something like this:

![road test world](/img/cs373/unit1/road_test_world.png)


With that in mind, I wrote the road test:

```python
import localisation         # the module with the robot code

def road_test():
        world = [['black', 'yellow', 'black', 'yellow', 'black', 'white'],
                 ['black', 'black', 'black', 'black', 'black', 'white'],
                 ['black', 'yellow', 'black', 'yellow', 'black', 'white'],
                 ['black', 'black', 'black', 'black', 'black', 'white'],
                 ['black', 'yellow', 'black', 'yellow', 'black', 'white'],
                 ['black', 'black', 'black', 'black', 'black', 'white']]  
        sensor = localisation.Sensor(0.8)
        robot = localisation.Robot2D(world, sensor, 0.6)
        print 'robot initalised.'
        
        motion = [[0, 0], [-1, 0], [0, -1], [0, -1], [0, -1], [-1, 0]]
        measurements = ['white', 'white', 'black', 'yellow', 'black', 'black']
        print 'driving!'
        robot.localise(motion, measurements)
        print translate_drive(motion, measurements)

        guess = robot.locate()['guess']
        q = [(i, j) for p, j, i in guess]
		q = [i == 2 for i, j in q]
		return robot
```

The `translate_drive` function maps the `motions` and `measurements`
into a pretty output form to make it more intuitive as to what's being
simulated:

```python
def translate_drive(motion_seq, readings):
    directions = {
                    '[0, 0]':   'nowhere',
                    '[0, 1]':   'right',
                    '[0, -1]':  'left',
                    '[1, 0]':   'down',
                    '[-1, 0]':  'up'
    }

    drive_str = ''
    for i in range(len(motion_seq)):
        motion = motion_seq[i]
        reading = readings[i]
        drive_str += '\tgo ' + directions[str(motion)]
        drive_str += ', saw ' + reading + '\n'

    return drive_str
```

Here's an example of the road test in action:

![road test screenshot](/img/cs373/unit1/road_test.png)

As expected, the robot thinks it is somewhere in the middle lane.

Next week, we start learning
[particle filters](https://en.wikipedia.org/wiki/Particle_filter)
and [kalman filters](https://en.wikipedia.org/wiki/Kalman_filter).

I'm actively working to develop a physical AGV (autonomous ground
vehicle). The objective is to develop a platform I can use to build
later, more practical robots on; the AGV platform will be focused on
navigation. As part of this task, I'm also working to translate the
Python code to C++ (suitable for the Arduino, for example).

Stay tuned!

### References:

* [Robust Monte Carlo Localization for Mobile Robots](http://robots.stanford.edu/papers/thrun.robust-mcl.html)
* [Python versions of the repo)](https://bitbucket.org/kisom/cs373) (private until the 28th)
* [C++ version](https://github.com/kisom/cs373)
