# `reflect-bench`

Another piece of code inspired by a SpigotMC post that I
decided to upload just to pad my repo count.

This user [wanted to know](https://www.spigotmc.org/threads/reflection-problem-of-spigot.436331/#post-3793005)
if replacing reflection-based event handler calling with
an interface/observer-based calling would do anything for
performance. I thought it would be a good idea to see what
the numbers say about the proposition.

# Results

```
Benchmark                         Mode  Cnt   Score   Error  Units
ReflectBench.baseline               avgt    5   0.598 ? 0.006  ns/op
ReflectBench.invokedynamic          avgt    5   3.244 ? 0.075  ns/op
ReflectBench.invokedynamicAndBox    avgt    5   6.242 ? 0.266  ns/op
ReflectBench.invokedynamicNoReturn  avgt    5   1.971 ? 0.322  ns/op
ReflectBench.methodHandle           avgt    5   9.152 ? 0.080  ns/op
ReflectBench.reflection             avgt    5  10.169 ? 0.131  ns/op
ReflectBench.reflectionNoReturn     avgt    5   4.518 ? 0.051  ns/op
```

# Analysis

The two most important measurements are
`invokedynamicNoReturn` and `reflectionNoReturn` as they
present the most realistic gains that could be made calling
void-returning event handlers. The baseline and returning
methods are presented as control measurements to ensure
that the times measured are not impacted by the JIT.

The test found that raw calls are ~2 ns compared to ~4.5 ns
or just over 2x slower. However, this is obviously a flawed
way of viewing the results because switching all event
handler calls to a raw call will not result in a 2x speedup
in this area because the reflective overhead itself is only
a portion of the cost of the event handler; what the
invoked method does is far more important than the overhead
of reflection itself. The more work done, the smaller of a
benefit eliminating reflection does for the performance of
the server. The best comparison would be absolute, this
is ~2.5 ns penalty for reflection versus raw call.

One of my suggestions to retaining the same structure using
traditional reflection is either using raw bytecode-based
instrumentation in order to get around the invocation
overhead (which is a technique I'm aware Paper utilizes)
or perhaps using MethodHandles instead. As it turns out,
this confers ~1 ns benefit over vanilla reflection, which,
while it does not nullify the reflective penalty, does get
within ~1.5 ns of a raw call.

# Credits

Built with [IntelliJ IDEA](https://www.jetbrains.com/idea/)

Uses [JMH](https://openjdk.java.net/projects/code-tools/jmh/)
