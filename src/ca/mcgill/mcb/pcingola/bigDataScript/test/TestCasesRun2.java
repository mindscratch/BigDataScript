package ca.mcgill.mcb.pcingola.bigDataScript.test;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.bigDataScript.util.Gpr;

/**
 * Test cases that require BDS code execution and check results
 *
 * Note: These test cases requires that the BDS code is correctly parsed, compiled and executes.
 *
 * @author pcingola
 *
 */
public class TestCasesRun2 extends TestCasesBase {

	@Test
	public void test100() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("s", 1);
		expectedValues.put("s2", -1);

		runAndCheckMultiple("test/run_100.bds", expectedValues);
	}

	@Test
	public void test101() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("a", 1);
		expectedValues.put("b", 3);
		expectedValues.put("c", 5);

		runAndCheckMultiple("test/run_101.bds", expectedValues);
	}

	@Test
	public void test102() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("a", 1);
		expectedValues.put("b", 3);
		expectedValues.put("c", 5);
		expectedValues.put("d", 0);

		runAndCheckMultiple("test/run_102.bds", expectedValues);
	}

	@Test
	public void test103() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("is", "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]");
		expectedValues.put("is2", "[1, 3, 5, 7, 9]");
		expectedValues.put("rs", "[1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0]");

		runAndCheckMultiple("test/run_103.bds", expectedValues);
	}

	@Test
	public void test104() {
		runAndCheck("test/run_104.bds", "isRun", "true");
	}

	@Test
	public void test105() {
		runAndCheck("test/run_105.bds", "isRun", "false");
	}

	@Test
	public void test106() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("str1", "l[1] : '2'");
		expectedValues.put("str2", "m{'Hello'} : 'Bye'");

		runAndCheckMultiple("test/run_106.bds", expectedValues);
	}

	@Test
	public void test107() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("paramName", "parameter_value");
		expectedValues.put("file1", "/path/to/file_1.txt");
		expectedValues.put("file2", "/path/to/file_2.txt");
		expectedValues.put("file3", "/path/to/file_3.txt");
		expectedValues.put("file4", "/path/to/file_4.txt");
		expectedValues.put("file5", "/path/to/file_5.txt");

		runAndCheckMultiple("test/run_107.bds", expectedValues);
	}

	@Test
	public void test108() {
		HashMap<String, Object> expectedValues = new HashMap<String, Object>();
		expectedValues.put("paramName", "parameter_value");
		expectedValues.put("file1", "/path/to/file_1.txt");
		expectedValues.put("file2", "/path/to/file_2.txt");
		expectedValues.put("file3", "/path/to/file_3.NEW.txt");
		expectedValues.put("file4", "/path/to/file_4.txt");
		expectedValues.put("file5", "/path/to/file_5.NEW.txt");

		runAndCheckMultiple("test/run_108.bds", expectedValues);
	}

	public void test109() {
		runAndCheck("test/run_109.bds", "r1", "4027146782649399912");
	}

	public void test110() {
		runAndCheck("test/run_110.bds", "runOk", "true");
	}

	public void test111() {
		runAndCheck("test/run_111.bds", "runOk", "false");
	}

	public void test112() {
		runAndCheck("test/run_112.bds", "runOk", "false");
	}

	@Test
	public void test113_parallel_function_calls() {
		String stdout = runAndReturnStdout("test/run_113.bds");

		Set<String> linesPar = new HashSet<String>();
		for (String line : stdout.split("\n")) {
			if (line.startsWith("Par:")) {
				if (linesPar.contains(line)) throw new RuntimeException("Line repeated (this should never happen): '" + line + "'");
				linesPar.add(line);
			}
		}
	}

	@Test
	public void test114_parallel_function_task_calls() {
		String stdout = runAndReturnStdout("test/run_114.bds");

		Set<String> linesPar = new HashSet<String>();
		for (String line : stdout.split("\n"))
			if (line.startsWith("TASK")) linesPar.add(line);

		// Check
		Assert.assertTrue("There should be 5 tasks", linesPar.size() == 5);
	}

	@Test
	public void test115_task_dependency_using_taskId() {
		String stdout = runAndReturnStdout("test/run_115.bds");
		Assert.assertEquals("Hi 1\nBye 1\nHi 2\nBye 2\n", stdout);
	}

	@Test
	public void test116_lineWrap_backslashId() {
		String stdout = runAndReturnStdout("test/run_116.bds");
		Assert.assertEquals("hi bye\nThe answer\t\tis: 42", stdout);
	}

	@Test
	public void test117_serial_parallel_tasks() {
		String expectedStdout = "Iter 1, Task 1: End\n" //
				+ "Iter 1, Task 2: Start\n" //
				+ "Iter 1, Task 2: End\n" //
				+ "Iter 5, Task 1: End\n" //
				+ "Iter 5, Task 2: Start\n" //
				+ "Iter 5, Task 2: End\n" //
				;;

				String stdout = runAndReturnStdout("test/run_117.bds");

				if (stdout.indexOf(expectedStdout) < 0) {
					String msg = "Cannot find desired output:\n" //
							+ "---------- Expected output ----------\n" //
							+ expectedStdout //
							+ "-------------- STDOUT --------------\n" //
							+ stdout //
							;
					System.err.println(msg);
					throw new RuntimeException(msg);
				}
	}

	@Test
	public void test118_dependency_using_path() {
		runAndCheckExit("test/run_118.bds", 0);
	}

	@Test
	public void test119_task_dependency() {
		Gpr.debug("Test");

		// Delete input file
		String inFile = "tmp_in.txt";
		(new File(inFile)).delete();

		String expectedStdout1 = "Creating tmp_in.txt\n" //
				+ "Running task\n" //
				+ "Creating tmp_out.txt\n" //
				+ "Done\n" //"
				;

		String expectedStdout2 = "Running task\n" //
				+ "Done\n" //"
				;

		System.out.println("First run:");
		runAndCheckStdout("test/run_119.bds", expectedStdout1);

		System.out.println("\n\nSecond run:");
		runAndCheckStdout("test/run_119.bds", expectedStdout2);
	}

	@Test
	public void test120_split_empty_string() {
		runAndCheck("test/run_120.bds", "len", "0");
	}

	@Test
	public void test121_split_fail_regex() {
		runAndCheck("test/run_121.bds", "len", "1");
	}

	@Test
	public void test122_nestest_break_continue() {
		runAndCheck("test/run_122.bds", "out", "5\t7");
	}

	@Test
	public void test123_literals_sys_task() {
		Gpr.debug("Test");

		String output = "print_quote        |\\t|\n" //
				+ "print_quote        |\\t|    variable:$hi\n" //
				+ "print_double       |\t|\n" //
				+ "print_double       |\t|    variable:Hello\n" //
				+ "print_double_esc   |\\t|\n" //
				+ "print_double_esc   |\\t|   variable:Hello\n" //
				+ "sys                |\\t|\n" //
				+ "sys                |\\t|    variable:Hello\n" //
				+ "task               |\\t|\n" //
				+ "task               |\\t|    variable:Hello\n" //
				;

		runAndCheckStdout("test/run_123.bds", output);
	}

	@Test
	public void test124_quiet_mode() {
		Gpr.debug("Test");
		String output = "print 0\n" //
				+ "print 1\n" //
				+ "print 2\n" //
				+ "print 3\n" //
				+ "print 4\n" //
				+ "print 5\n" //
				+ "print 6\n" //
				+ "print 7\n" //
				+ "print 8\n" //
				+ "print 9\n" //
				;

		// Run and capture stdout
		String args[] = { "-quiet", "test/run_124.bds" };
		String stdout = runAndReturnStdout(args);
		if (verbose) System.err.println("STDOUT: " + stdout);

		// Check that sys and task outputs are not there
		Assert.assertTrue("Print output should be in STDOUT", stdout.contains(output));
		Assert.assertTrue("Task output should NOT be in STDOUT", !stdout.contains("task"));
		Assert.assertTrue("Sys output should NOT be in STDOUT", !stdout.contains("sys"));
	}

	@Test
	public void test125_automatic_help() {
		Gpr.debug("Test");
		String output = "Command line options 'run_125.bds' :\n" //
				+ "\t-mean <int>                            : Help for argument 'mean' should be printed here\n" //
				+ "\t-min <int>                             : Help for argument 'min' should be printed here\n" //
				+ "\t-num <int>                             : Number of times 'hi' should be printed\n" //
				+ "\t-salutation <string>                   : Salutation to use\n" //
				+ "\t-someVeryLongCommandLineArgumentName   : This command line argument has a really long name\n" //
				+ "\t-useTab                                : Use tab before printing line\n" //
				+ "\n" //
				;

		// Run and capture stdout
		String args[] = { "test/run_125.bds", "-h" };
		String stdout = runAndReturnStdout(args);
		if (verbose) System.err.println("STDOUT: " + stdout);

		// Check that sys and task outputs are not there
		Assert.assertEquals(output, stdout);
	}

	/**
	 * Task dependent on output from a scheduled task
	 */
	@Test
	public void test126_task_dependency_scheduled() {
		Gpr.debug("Test");

		String expectedOutput = "IN: /Users/pcingola/zzz/in.txt\n" //
				+ "OUT: /Users/pcingola/zzz/out.txt\n" //
				+ "OUT_0: /Users/pcingola/zzz/out_0.txt\n" //
				+ "OUT_1: /Users/pcingola/zzz/out_1.txt\n" //
				+ "OUT_2: /Users/pcingola/zzz/out_2.txt\n" //
				+ "    OUT_0_0: /Users/pcingola/zzz/out_0_0.txt\n" //
				+ "    OUT_0_1: /Users/pcingola/zzz/out_0_1.txt\n" //
				+ "    OUT_0_2: /Users/pcingola/zzz/out_0_2.txt\n" //
				+ "    OUT_1_0: /Users/pcingola/zzz/out_1_0.txt\n" //
				+ "    OUT_1_1: /Users/pcingola/zzz/out_1_1.txt\n" //
				+ "    OUT_1_2: /Users/pcingola/zzz/out_1_2.txt\n" //
				+ "    OUT_2_0: /Users/pcingola/zzz/out_2_0.txt\n" //
				+ "    OUT_2_1: /Users/pcingola/zzz/out_2_1.txt\n" //
				+ "    OUT_2_2: /Users/pcingola/zzz/out_2_2.txt\n" //
				;

		String stdout = runAndReturnStdout("test/run_126.bds");
		if (verbose) System.err.println("STDOUT: " + stdout);

		// Check that task output lines
		for (String out : expectedOutput.split("\n")) {
			Assert.assertTrue("Expected output line not found: '" + out + "'", stdout.contains(out));
		}
	}

	@Test
	public void test127_interpolate_variable_with_underscores() {
		Gpr.debug("Test");

		String output = "bwa parameters\n" //
				+ "bwa parameters\n" //
				+ "bwa parameters\n" //
				+ "bwa parameters\n" //
				;

		runAndCheckStdout("test/run_127.bds", output);
	}

	@Test
	public void test128_task_local_variables() {
		Gpr.debug("Test");
		runAndCheckStdout("test/run_128.bds", "TEST\n");
	}

	@Test
	public void test129_chdir_sys() {
		String out = runAndReturnStdout("test/run_129.bds");
		Assert.assertTrue(out.contains("FILE_01\n"));
		Assert.assertTrue(out.contains("FILE_02\n"));
	}

	@Test
	public void test130_chdir_task() {
		String out = runAndReturnStdout("test/run_130.bds");
		Assert.assertTrue(out.contains("FILE_01\n"));
		Assert.assertTrue(out.contains("FILE_02\n"));
	}

	@Test
	public void test131_chdir_fileMethods() {
		String out = ""//
				+ "chdir_test_file_01.txt\tread:FILE_01\n" //
				+ "chdir_test_file_01.txt\treadLines:[FILE_01]\n" //
				+ "chdir_test_file_01.txt\texists:true\n" //
				+ "chdir_test_file_01.txt\tisDir:false\n" //
				+ "chdir_test_file_01.txt\tisEmpty:false\n" //
				+ "chdir_test_file_01.txt\tisFile:true\n" //
				+ "chdir_test_file_01.txt\tcanRead:true\n" //
				+ "chdir_test_file_01.txt\tcanWrite:true\n" //
				+ "\n" //
				+ "----------\n" //
				+ "chdir_test_file_02.txt\tread:FILE_02\n" //
				+ "chdir_test_file_02.txt\treadLines:[FILE_02]\n" //
				+ "chdir_test_file_02.txt\texists:true\n" //
				+ "chdir_test_file_02.txt\tisDir:false\n" //
				+ "chdir_test_file_02.txt\tisEmpty:false\n" //
				+ "chdir_test_file_02.txt\tisFile:true\n" //
				+ "chdir_test_file_02.txt\tcanRead:true\n" //
				+ "chdir_test_file_02.txt\tcanWrite:true\n" //
				;

		String outreal = runAndReturnStdout("test/run_131.bds");
		Assert.assertEquals(out, outreal);
	}

}
