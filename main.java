public class main {
  public static void main(String[] args) {
    int[] ages = { 20, 18, 24, 13, 45 };
    // float avg, sum = 0;
    int len = ages.length;
    int lowestAge = ages[0];
    for (int age : ages) {
      // sum += age;
      if (lowestAge > age) {
        lowestAge = age;
      }
    }
    // avg = sum / len;
    // System.out.println("The average of age is :" + avg);
    System.out.println("The lowest age is:" + lowestAge);
  }
}
