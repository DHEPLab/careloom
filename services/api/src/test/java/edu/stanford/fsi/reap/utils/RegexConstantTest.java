package edu.stanford.fsi.reap.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegexConstantTest {
  @Test
  public void testPhoneRegexValidCases() {
    assertTrue("12345".matches(RegexConstant.PHONE_REGEX));
    assertTrue("1234567890".matches(RegexConstant.PHONE_REGEX));
    assertTrue("12345678901234567890".matches(RegexConstant.PHONE_REGEX));
  }

  @Test
  public void testPhoneRegexInvalidCases() {
    assertFalse("1234".matches(RegexConstant.PHONE_REGEX));
    assertFalse("123456789012345678901".matches(RegexConstant.PHONE_REGEX));
    assertFalse("12345abcde".matches(RegexConstant.PHONE_REGEX));
    assertFalse("12345-67890".matches(RegexConstant.PHONE_REGEX));
  }

  @Test
  public void testNameRegexValidCases() {
    assertTrue("John Doe".matches(RegexConstant.NAME_REGEX));
    assertTrue("韩梅梅".matches(RegexConstant.NAME_REGEX));
    assertTrue("よういち すぎはら".matches(RegexConstant.NAME_REGEX));
    assertTrue("O'Connor".matches(RegexConstant.NAME_REGEX));
    assertTrue("Álvaro López".matches(RegexConstant.NAME_REGEX));
    assertTrue("Marie-Claire".matches(RegexConstant.NAME_REGEX));
    assertTrue("José María".matches(RegexConstant.NAME_REGEX));
    assertTrue("Jean-Luc Picard".matches(RegexConstant.NAME_REGEX));
  }

  @Test
  public void testNameRegexInvalidCases() {
    assertFalse("".matches(RegexConstant.NAME_REGEX));
    assertFalse("a".repeat(51).matches(RegexConstant.NAME_REGEX));
    assertFalse("John@Doe".matches(RegexConstant.NAME_REGEX));
    assertFalse("1234".matches(RegexConstant.NAME_REGEX));
    assertFalse("John_Doe".matches(RegexConstant.NAME_REGEX));
    assertFalse("Jean$Luc".matches(RegexConstant.NAME_REGEX));
  }
}
