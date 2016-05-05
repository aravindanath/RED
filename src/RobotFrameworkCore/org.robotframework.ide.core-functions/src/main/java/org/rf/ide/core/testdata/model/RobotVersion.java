/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

public final class RobotVersion implements Comparable<RobotVersion> {

    public static final RobotVersion UNKNOWN = new RobotVersion(-1, -1);

    private final int major;

    private final int minor;

    private final Optional<Integer> patch;

    public static RobotVersion from(final String version) {
        if (version == null) {
            return UNKNOWN;
        }

        final Matcher matcher = Pattern.compile("(\\d)\\.(\\d+)(\\.(\\d+))?").matcher(version);
        if (matcher.find()) {
            if (matcher.group(4) == null) {
                return new RobotVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            } else {
                return new RobotVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(4)));
            }
        }
        throw new IllegalStateException("Unable to recognize Robot Framework version number");
    }

    public RobotVersion(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
        this.patch = Optional.absent();
    }

    public RobotVersion(final int major, final int minor, final int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = Optional.of(patch);
    }

    public boolean isEqualTo(final RobotVersion otherVersion) {
        if (otherVersion == UNKNOWN) {
            return this == UNKNOWN;
        }
        return major == otherVersion.major && minor == otherVersion.minor && patch.equals(otherVersion.patch);
    }

    public boolean isNotEqualTo(final RobotVersion otherVersion) {
        return !isEqualTo(otherVersion);
    }

    public boolean isOlderThan(final RobotVersion otherVersion) {
        if (otherVersion == UNKNOWN) {
            return false;
        }
        return major < otherVersion.major || (major == otherVersion.major && minor < otherVersion.minor)
                || (major == otherVersion.major && minor == otherVersion.minor
                        && isLessPatch(patch, otherVersion.patch));
    }

    public boolean isOlderThanOrEqualTo(final RobotVersion otherVersion) {
        return isOlderThan(otherVersion) || isEqualTo(otherVersion);
    }

    public boolean isNewerThan(final RobotVersion otherVersion) {
        if (otherVersion == UNKNOWN) {
            return false;
        }
        return major > otherVersion.major || (major == otherVersion.major && minor > otherVersion.minor)
                || (major == otherVersion.major && minor == otherVersion.minor
                        && isLessPatch(otherVersion.patch, patch));
    }

    public boolean isNewerOrEqualTo(final RobotVersion otherVersion) {
        return isNewerThan(otherVersion) || isEqualTo(otherVersion);
    }

    private boolean isLessPatch(final Optional<Integer> patch1, final Optional<Integer> patch2) {
        if (!patch1.isPresent() && !patch2.isPresent()) {
            return false;
        } else if (patch1.isPresent() && !patch2.isPresent()) {
            return false;
        } else if (!patch1.isPresent() && patch2.isPresent()) {
            return true;
        } else {
            final int p1 = patch1.get();
            final int p2 = patch2.get();
            return p1 < p2;
        }
    }

    @Override
    public int compareTo(final RobotVersion otherVersion) {
        if (isEqualTo(otherVersion)) {
            return 0;
        } else if (isOlderThan(otherVersion)) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RobotVersion) {
            final RobotVersion that = (RobotVersion) obj;
            return this.major == that.major && this.minor == that.minor && Objects.equals(this.patch, that.patch);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return String.format(getClass().getName() + "[major=%s, minor=%s, patch=%s]", this.major, this.minor,
                this.patch);
    }
}
