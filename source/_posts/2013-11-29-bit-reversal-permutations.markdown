---
layout: post
title: "Bit-Reversal Permutations"
date: 2013-11-29 19:59 MST
comments: false
categories: [hacking]
---

While I was writing my
[Go implementation](https://github.com/kisom/catena) of
[Catena](http://eprint.iacr.org/2013/525.pdf), I had to implement a
function to reverse the bit order of an integer. I couldn't find
anything online that talked about how to do it, but plenty on the case
covered in [Hacker's Delight](http://www.hackersdelight.org/); I
figure I should write up what I did and how I did it. A large part of
this was because it took me a little bit to figure out the proper name
for it.

### The Problem

Let me first distinguish between the two cases I'm looking for. Let's
consider the following numbers in our test case:

* `0x1` (in binary as an unsigned 16-bit integer: `0b0000000000000001`)
* `0x10` (`0b0000000000010000`)
* `0x100` (`0b0000000100000000`)
* `0xab` (`0b0000000010101011`)

Now, the normal case is to reverse the entire integer (i.e. the
Hacker's Delight case). In this case, these numbers reverse to:

* `0x1` -> `0b1000000000000000` -> `0x8000`
* `0x10` -> `0b0000100000000000` -> `0x800`
* `x100` -> `0b0000000010000000` -> `0x80`
* `0xab` -> `0b1101010100000000` -> `0xd500`

Now, we have to cover the concept of *bit length*. This refers to the
number of bits required to represent a number. For example, given our
example numbers:

* `0x1` has a bit length of 1
* `0x10` has a bit length of 5
* `0x100` has a bit length of 9
* `0xab` has a bit length of 8

In the τ function in Catena, we want numbers to be in reverse bit
order with their bit length; the paper calls this function a
*bit-reversal permutation*. Keeping our previous examples, what we're
looking for is

* `τ(0x1)` -> `τ(0b1)` -> `0b1` -> `0x1`
* `τ(0x10)` -> `τ(0b10000)` -> `0b00001` -> `0x1`
* `τ(0x100)` -> `τ(0b100000000)` -> `0b000000001` -> `0x1`
* `τ(0xab)` -> `τ(0b10101011)` -> `0b11010101` -> `0xd5`

### A Shiny but Naïve Solution

A naïve implementation, using Go's math standard library (which
provides the `BitLen` and `Bit` methods):

```
package naive

import (
        "math/big"
)

func tau(x uint32) int {
        var n uint
        var bigX = big.NewInt(int64(x))

        bitLen := bigX.BitLen()

        for i := 0; i < bitLen; i++ {
                n += (bigX.Bit(i) << uint(bitLen-i-1))
        }
        return int(n)
}
```

The sourcecode for these implementations can be found in
[hg.tyrfingr.is/kyle/revbits](http://hg.tyrfingr.is/kyle/revbits).

Here is a benchmark running through our four test cases:

```
~/code/go/src/hg.tyrfingr.is/kyle/revbits/naive
(0) <monalisa> $ go test -test.bench='.+'
PASS
BenchmarkTau     1000000              1321 ns/op
ok      hg.tyrfingr.is/kyle/revbits/naive       1.340s
```

When I first had this in the `catena` implementation, tests with a
garlic (a parameter controlling how much memory is used by the
package) of greater than 2 (which is entirely too low) were taking
more than ten minutes to run. Clearly, something was amiss. I had to
roll up my sleeves and add some profiling into it. One snag I ran into
was that, as a consequence of the test being killed early, running the
test with the `-cpuprofile` and `-memprofile` flags wouldn't give
anything useful; I wrote a quick test that would dump the profiles
after 9 minutes. At first, I suspected the SHA-256 function was taking
the most time, but the profiles clearly showed that my tau function
was to blame. I know it gets beaten into our collective brain, but
once again, it's critical to have numbers to target improvements; the
Go tools were extremely useful in narrowing down candidates for
targeted development.

While this implementation is short and succinct, it is unacceptable
for real-world use. For example, a more suitable garlic value is 16;
in the function, we use the value 2<sup>*garlic*</sup>; 2 is a long
ways from that.

To solve the problem, I broke it into two parts: calculating the bit
length of a number, and reversing the bits. To calculate the bit
length, we can find the leftmost bit position with a value of `1`. We
can do this by starting at the 31st bit, stepping through each bit
until we get to the rightmost bit. For each bit, we should check to
see if that bit is `1`. If it is, we return the current bit
number. Otherwise, we keep stepping through the bits.

Here's our first pass:

```
func bitLength(x uint32) uint32 {
        var i uint32 = 32

        for {
                if (x & (1 << i)) != 0 {
                        return i
                } else if i == 0 {
                        break
                }
                i--
        }
        return 0
}
```

It's very straightforward, and as we'll see shortly, considerably
faster than our naïve implementation. Now we can focus on the actual
`tau` function.

First, we determine how many bits we have for the input number. Then,
starting from 0, we check to see if the rightmost (or
least-significant bit) is 1. We take this value, and shift it to the
reverse bit position (the current bit index subtracted from the bit
length), and add that to our counter. In other words, if we have `bit
:= x & 1`, we can add it to our reversed value with `n += (bit <<
(bitLen - i))`. Finally, we need to shift our original number to the
right by one.

In code:

```
func tau(x uint32) int {
        var bitLen = bitLength(x)
        var n uint32
        for i := uint32(0); i <= bitLen; i++ {
                bit := x & 1
                x = x >> 1
                n += (bit << (bitLen - i))
        }
        return int(n)
}
```

We can use the exact same benchmark from the naive implementation to
test this version:

```
~/code/go/src/hg.tyrfingr.is/kyle/revbits/slowertau
(0) <monalisa> $ go test -test.bench='.+'
PASS
BenchmarkTau     5000000               314 ns/op
ok      hg.tyrfingr.is/kyle/revbits/slowertau   1.894s
```

This version runs in less than a quarter of the time as our naïve
implementation! In fact, this is enough to make `catena` usable (with
a garlic of 16, even):

```
~/code/go/src/github.com/gokyle/catena
(1) <ono-sendai> $ clear; go test -test.bench='.+'
PASS
BenchmarkBasicHash             1        1393076200 ns/op
ok      github.com/gokyle/catena        5.946s
```

However, it turns out we can actually speed this up. The key to
speeding this up is in the `bitLength` function. For every number,
from `0` to `4294967296`, we run through thirty-two iterations of the
loop. This adds up in `catena`; while we won't see a drastic
improvement, we can still shave some time off.

Our improvement is to cut down on the number of bits we use in our
initial index:

```
func bitLength(x uint32) uint32 {
        var i uint32 = 32

        switch {
        case x > 16777216:
                i = 32
        case x > 65536:
                i = 24
        case x > 256:
                i = 16
        default:
                i = 8
        }

        for {
                if (x & (1 << i)) != 0 {
                        return i
                } else if i == 0 {
                        break
                }
                i--
        }
        return 0
}
```

The first case in the switch checks for a number greater than
2<sup>24</sup>; in this case, we go through the full 32 bit
integers. The second case checks for a 24-bit integer, the third case
checks for a 16-bit integer, and by default, we treat the number as an
8-bit integer. In this case, we can cut the number of iterations down
significantly. How much does this improve our benchmark?

```
~/code/go/src/hg.tyrfingr.is/kyle/revbits/tau
(0) <monalisa> $ go test -test.bench='.+'
PASS
BenchmarkTau    20000000                92.1 ns/op
ok      hg.tyrfingr.is/kyle/revbits/tau 1.940s
```

This version runs in less than a third of the time of our previous
version, and less than a tenth of the time of the naïve version.

This final version, which is used in `catena`, gives us this
`catena` benchmark:

```
~/code/go/src/github.com/gokyle/catena
(0) <ono-sendai> $ go test -test.bench='.+'
PASS
BenchmarkBasicHash             1        1240838690 ns/op
ok      github.com/gokyle/catena        5.796s
```

I think writing this was the most fun I've had coding all week (and
possibly for the last couple weeks).
