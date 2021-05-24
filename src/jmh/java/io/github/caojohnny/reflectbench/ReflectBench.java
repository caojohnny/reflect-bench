package io.github.caojohnny.reflectbench;

import com.nesaak.noreflection.NoReflection;
import com.nesaak.noreflection.access.DynamicCaller;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ReflectBench {
    private static final Method incrMethod;
    private static final Method incrMethodNoReturn;
    private static final DynamicCaller incrDynCaller;
    private static final DynamicCaller incrDynCallerNoReturn;
    private static final MethodHandle incrMh;

    static {
        try {
            incrMethod = Counter.class.getMethod("incr");
            incrMethodNoReturn = Counter.class.getMethod("incrNoReturn");
            incrDynCaller = NoReflection.shared().get(incrMethod);
            incrDynCallerNoReturn = NoReflection.shared().get(incrMethodNoReturn);
            incrMh = MethodHandles.lookup().unreflect(incrMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Counter counter;

    @Setup
    public void setup() {
        this.counter = new Counter();
    }

    @Benchmark
    public void baseline() {
    }

    @Benchmark
    public int invokedynamic() {
        return this.counter.incr();
    }

    @Benchmark
    public Object reflectionAndBox() throws ReflectiveOperationException {
        return incrMethod.invoke(this.counter);
    }

    @Benchmark
    public Object dynCallerAndBox() {
        return incrDynCaller.call(this.counter);
    }

    @Benchmark
    public Object mh() throws Throwable {
        return incrMh.invoke(this.counter);
    }

    @Benchmark
    public Object invokedynamicAndBox() {
        return this.counter.incr();
    }

    @Benchmark
    public void invokedynamicNoReturn() {
        this.counter.incrNoReturn();
    }

    @Benchmark
    public void reflectionNoReturn() throws ReflectiveOperationException {
        incrMethodNoReturn.invoke(this.counter);
    }

    @Benchmark
    public void dynCallerNoReturn() {
        incrDynCallerNoReturn.call(this.counter);
    }

    private static class Counter {
        private int count;

        public int incr() {
            return this.count++;
        }

        public void incrNoReturn() {
            this.count++;
        }
    }
}
