module lang::sdf2::Plugin

import lang::c90::syntax::C;
import util::IDE;
import ParseTree;

public TranslationUnit parseTU(str input, loc l) {
  return parse(#TranslationUnit, input, l);
}

public void main() {
  registerLanguage("C program", "c", parseTU);
  registerLanguage("C header", "h", parseTU);
}