plugins {
    id("frs.neoforge")
}

dependencies {
    // javax.annotation.Nonnull/Nullable, which the command code uses throughout. Forge and
    // NeoForge both ship it at runtime, but not on every node's compile classpath.
    "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")
}
