/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdkmanager;


import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.SdkManagerTestCase;
import com.android.utils.ILogger;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * Tests the SDK Manager command-line tool using a mock "black box"
 * approach by invoking the main with a given command line and then
 * checking the results.
 *
 * @see SdkManagerTest SdkManagerTest for testing internals methods directly.
 */
public class SdkManagerTest2 extends SdkManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // add 2 tag/abi folders with a new skin each
        File siX86 = makeSystemImageFolder(TARGET_DIR_NAME_0, "tag-1", "x86");
        makeFakeSkin(siX86, "Tag1X86Skin");
        File siArm = makeSystemImageFolder(TARGET_DIR_NAME_0, "tag-1", "armeabi");
        makeFakeSkin(siArm, "Tag1ArmSkin");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // ------ tests

    public void testAvdTargets() {
        runCmdLine("list", "targets");
        assertEquals(
                "P Available Android targets:\n" +
                "P ----------\n" +
                "P id: 1 or \"android-0\"\n" +
                "P      Name: Android 0.0\n" +
                "P      Type: Platform\n" +
                "P      API level: 0\n" +
                "P      Revision: 1\n" +
                "P      Skins: HVGA (default), Tag1ArmSkin, Tag1X86Skin\n" +
                "P  Tag/ABIs : default/armeabi, tag-1/armeabi, tag-1/x86\n",
                getLog().toString());

        runCmdLine("list", "targets", "--compact");
        assertEquals(
                "P android-0\n",
                getLog().toString());
    }

    public void testAvdList_Empty() {
        runCmdLine("list", "avd");
        assertEquals("P Available Android Virtual Devices:\n", getLog().toString());

        runCmdLine("list", "avd", "--compact");
        assertEquals("", getLog().toString());
    }

    public void testCreateAvd() {
        runCmdLine("list", "avd");
        assertEquals("P Available Android Virtual Devices:\n", getLog().toString());

        runCmdLine("create", "avd",
                "--target", "android-0",
                "--name",   "my-avd",
                "--abi",    "armeabi");
        assertEquals(
                "P Android 0.0 is a basic Android platform.\n" +
                "P Do you wish to create a custom hardware profile [no]" +
                "Created AVD 'my-avd' based on Android 0.0, ARM (armeabi) processor\n",
                getLog().toString());

        runCmdLine("create", "avd",
                "--target", "android-0",
                "--name",   "my-avd2",
                "--abi",    "default/armeabi");
        assertEquals(
                "P Android 0.0 is a basic Android platform.\n" +
                "P Do you wish to create a custom hardware profile [no]" +
                "Created AVD 'my-avd2' based on Android 0.0, ARM (armeabi) processor\n",
                getLog().toString());

        runCmdLine("create", "avd",
                "--target", "android-0",
                "--name",   "avd-for-tag1",
                "--abi",    "tag-1/armeabi");
        assertEquals(
                "P Android 0.0 is a basic Android platform.\n" +
                "P Do you wish to create a custom hardware profile [no]" +
                "Created AVD 'avd-for-tag1' based on Android 0.0, Tag 1 ARM (armeabi) processor\n",
                getLog().toString());

        runCmdLine("create", "avd",
                "--target", "android-0",
                "--name",   "avd-for-tag2",
                "--tag",    "tag-1",
                "--abi",    "armeabi");
        assertEquals(
                "P Android 0.0 is a basic Android platform.\n" +
                "P Do you wish to create a custom hardware profile [no]" +
                "Created AVD 'avd-for-tag2' based on Android 0.0, Tag 1 ARM (armeabi) processor\n",
                getLog().toString());

        runCmdLine("list", "avd");
        assertEquals(
                "P Available Android Virtual Devices:\n" +
                "P     Name: avd-for-tag1\n" +
                "P     Path: @AVD/avd-for-tag1.avd\n" +
                "P   Target: Android 0.0 (API level 0)\n" +
                "P  Tag/ABI: tag-1/armeabi\n" +
                "P     Skin: HVGA\n" +
                "P ---------\n" +
                "P     Name: avd-for-tag2\n" +
                "P     Path: @AVD/avd-for-tag2.avd\n" +
                "P   Target: Android 0.0 (API level 0)\n" +
                "P  Tag/ABI: tag-1/armeabi\n" +
                "P     Skin: HVGA\n" +
                "P ---------\n" +
                "P     Name: my-avd\n" +
                "P     Path: @AVD/my-avd.avd\n" +
                "P   Target: Android 0.0 (API level 0)\n" +
                "P  Tag/ABI: default/armeabi\n" +
                "P     Skin: HVGA\n" +
                "P ---------\n" +
                "P     Name: my-avd2\n" +
                "P     Path: @AVD/my-avd2.avd\n" +
                "P   Target: Android 0.0 (API level 0)\n" +
                "P  Tag/ABI: default/armeabi\n" +
                "P     Skin: HVGA\n",
                sanitizePaths(getLog().toString()));
    }

    public void testCreateAvd_Errors() {
        expectExitOnError("E The parameter --target must be defined for action 'create avd'",
                "create", "avd",
                "--name",   "my-avd",
                "--abi",    "armeabi");

        expectExitOnError("E The parameter --name must be defined for action 'create avd'",
                "create", "avd",
                "--target", "android-0",
                "--abi",    "armeabi");

        expectExitOnError("E This platform has more than one ABI. Please specify one using --abi",
                "create", "avd",
                "--target", "android-0",
                "--name",   "my-avd");

        expectExitOnError("E Invalid --abi abi/too/long: expected format 'abi' or 'tag/abi'",
                "create", "avd",
                "--target", "android-0",
                "--name",   "my-avd",
                "--abi",    "abi/too/long");

        expectExitOnError("E --tag tag-1 conflicts with --abi other-tag/armeabi",
                "create", "avd",
                "--target", "android-0",
                "--name",   "my-avd",
                "--tag",    "tag-1",
                "--abi",    "other-tag/armeabi");

        expectExitOnError("E Invalid --tag not-a-tag for the selected target",
                "create", "avd",
                "--target", "android-0",
                "--name",   "my-avd",
                "--tag",    "not-a-tag",
                "--abi",    "armeabi");

        expectExitOnError("E Invalid --abi not-an-abi for the selected target",
                "create", "avd",
                "--target", "android-0",
                "--name",   "my-avd",
                "--tag",    "tag-1",
                "--abi",    "not-an-abi");
    }

    // ------ helpers

    /**
     * A test-specific implementation of {@link SdkCommandLine} that calls
     * {@link Assert#fail()} instead of {@link System#exit(int)}.
     */
    private static class ExitSdkCommandLine extends SdkCommandLine {
        public ExitSdkCommandLine(ILogger logger) {
            super(logger);
        }

        @Override
        protected void exit() {
            fail("SdkCommandLine.exit reached. Log:\n" + getLog().toString());
        }
    }

    /**
     * A test-specific implementation of {@link Main} that calls
     * {@link Assert#fail()} instead of {@link System#exit(int)}.
     */
    private static class ExitMain extends Main {
        @Override
        protected void exit(int code) {
            fail("Main.exit(" + code + ") reached. Log:\n" + getLogger().toString());
        }

        @Override
        protected String readLine(byte[] buffer) throws IOException {
            String log = getLogger().toString();

            if (log.endsWith("Do you wish to create a custom hardware profile [no]")) {
                return "no";
            }

            fail("Unexpected Main.readLine call. Log:\n" + log);
            return null; // not reached
        }
    }

    /**
     * Creates a {@link Main}, set it up with an {@code SdkManager} and a logger,
     * parses the given command-line arguments and executes the action.
     */
    private Main runCmdLine(String...args) {
        Main main = new ExitMain();
        main.setupForTest(getSdkManager(), getLog(), new ExitSdkCommandLine(getLog()), args);
        getLog().clear();
        main.doAction();
        return main;
    }

    /**
     * Used to invoke a test that should end by a fatal error (one severe enough for the
     * {@code Main} processor to just give up and call {@code exit}.)
     * <p/>
     * Invokes {@link #runCmdLine(String...)} with the given command-line arguments
     * and checks the log results to make sure the log output contains the expected string.
     */
    private void expectExitOnError(String expected, String...args) {
        boolean failedAsExpected = false;
        boolean failedToFailed = false;
        try {
            runCmdLine(args);
            failedToFailed = true;
        } catch (AssertionFailedError e) {
            String msg = e.getMessage();
            failedAsExpected = msg.contains(expected);
        }
        if (!failedAsExpected || failedToFailed) {
            fail("Expected exit-on-error, " +
                    (failedToFailed ? "did not fail at all." : "did not fail with expected string.") +
                    "\nExpected  : " + expected +
                    "\nActual log: " + getLog().toString());
        }
    }

    /**
     * Sanitizes paths to the SDK and the AVD root folders in the log output.
     */
    private String sanitizePaths(String str) {
        if (str != null) {
            String osPath = getSdkManager().getLocation();
            str = str.replace(osPath, "@SDK");

            try {
                osPath = getAvdManager().getBaseAvdFolder();
                str = str.replace(osPath, "@AVD");
            } catch (AndroidLocationException ignore) {}

            str = str.replace(File.separatorChar, '/');
        }

        return str;
    }

}
