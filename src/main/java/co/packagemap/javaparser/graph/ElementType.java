package co.packagemap.javaparser.graph;

class ElementType {
  Type caller;
  String name;
  String element;

  ElementType(Type caller, String name, String element) {
    this.caller = caller;
    this.name = name;
    this.element = element;
  }

  @Override
  public String toString() {
    return "ElementType [name=" + name + ", element=" + element + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((element == null) ? 0 : element.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ElementType other = (ElementType) obj;
    if (element == null) {
      if (other.element != null) return false;
    } else if (!element.equals(other.element)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }
}
