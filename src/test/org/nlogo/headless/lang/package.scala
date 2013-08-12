// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.lang

/**
 * Finder knows where in the filesystem .txt files containing tests are stored.  (It also
 * knows how to ignore tests that use features that aren't available.)
 *
 * Finder calls Parser to read the files and parse them into instances of LanguageTest; these
 * instances are just pure data.
 *
 * Fixture can actually run a LanguageTest. The fixture encapsulates all the messy details of
 * interacting with HeadlessWorkspace, including setup and teardown.
 *
 * Suites that need exactly one Fixture per test may extend FixtureSuite.
 *
 * Fixture and FixtureSuite are also useful even if you don't use Finder, Parser, and LanguageTest;
 * several suites in this directory use Fixture/FixtureSuite directly, rather than reading tests
 * out of .txt files.
 */

package object lang
