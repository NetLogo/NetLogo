// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

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
 * Suites that need exactly one Fixture per test may extend FixtureSuite.  Fixture and FixtureSuite
 * are also useful even if you don't use Finder, Parser, and LanguageTest.  The tests in the misc
 * subirectory use Fixture/FixtureSuite directly, rather than reading tests out of .txt files.  Some
 * of them also use ModelCreator, which lets you make a model with widgets in code, instead of
 * having to keep an actual .nlogo file around.
 *
 */

package object lang
