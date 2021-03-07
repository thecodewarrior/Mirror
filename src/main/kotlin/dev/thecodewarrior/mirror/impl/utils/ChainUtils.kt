package dev.thecodewarrior.mirror.impl.utils

internal inline class HashChain(val hash: Int) {
    constructor() : this(0)

    inline fun chain(value: Any?): HashChain {
        return HashChain(hash * 31 + value.hashCode())
    }

    inline fun finish(): Int = hash
}

internal inline class ComparatorChain(val comparison: Int) {
    constructor() : this(0)

    inline fun chain(value: Int): ComparatorChain {
        if(this.comparison != 0)
            return this
        return ComparatorChain(value)
    }

    inline fun chain(block: () -> Int): ComparatorChain {
        if(this.comparison != 0)
            return this
        return ComparatorChain(block())
    }

    inline fun chain(block: (ComparatorChain) -> ComparatorChain): ComparatorChain {
        if(this.comparison != 0)
            return this
        return block(this)
    }

    inline fun <T, F: Comparable<F>> chain(a: T, b: T, factor: (T) -> F): ComparatorChain {
        if(this.comparison != 0)
            return this
        return ComparatorChain(factor(a).compareTo(factor(b)))
    }

    inline fun <T, F: Comparable<F>> chainEach(a: Collection<T>, b: Collection<T>, factor: (T) -> F): ComparatorChain {
        if(this.comparison != 0)
            return this
        if(a.size != b.size) {
            return ComparatorChain(a.size.compareTo(b.size))
        } else {
            var v = this
            val iterA = a.iterator()
            val iterB = b.iterator()
            while(iterA.hasNext() && iterB.hasNext()) {
                v = v.chain(iterA.next(), iterB.next(), factor)
            }
            // if somehow they actually were uneven
            if(iterB.hasNext())
                return ComparatorChain(1)
            if(iterA.hasNext())
                return ComparatorChain(-1)
            return v
        }
    }

    inline fun <T, F: Comparable<F>> chainEach(a: Iterable<T>, b: Iterable<T>, factor: (T) -> F): ComparatorChain {
        if(this.comparison != 0)
            return this
        var v = this
        val iterA = a.iterator()
        val iterB = b.iterator()
        while(iterA.hasNext() && iterB.hasNext()) {
            v = v.chain(iterA.next(), iterB.next(), factor)
        }
        if(iterB.hasNext())
            return ComparatorChain(1)
        if(iterA.hasNext())
            return ComparatorChain(-1)
        return v
    }

    inline fun <T, F: Comparable<F>> chainEach(a: Array<T>, b: Array<T>, factor: (T) -> F): ComparatorChain {
        if(this.comparison != 0)
            return this
        if(a.size != b.size) {
            return ComparatorChain(a.size.compareTo(b.size))
        } else {
            var v = this
            for(i in a.indices) {
                v = v.chain(a[i], b[i], factor)
            }
            return v
        }
    }

    inline fun finish(): Int = comparison
}
