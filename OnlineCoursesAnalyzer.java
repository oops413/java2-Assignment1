import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {

    public static List<Course> courseList = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) throws IOException {
        courseList = Files.lines(Paths.get(datasetPath))
                .map(l -> l.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")).skip(1)
                .map(a -> new Course(a[0], a[1], a[2], a[3], a[4], a[5],
                        Integer.parseInt(a[6]), Integer.parseInt(a[7]), Integer.parseInt(a[8]),
                        Integer.parseInt(a[9]), Integer.parseInt(a[10]), Double.parseDouble(a[11]),
                        Double.parseDouble(a[12]), Double.parseDouble(a[13]),
                        Double.parseDouble(a[14]), Double.parseDouble(a[15]),
                        Double.parseDouble(a[16]), Double.parseDouble(a[17]),
                        Double.parseDouble(a[18]), Double.parseDouble(a[19]),
                        Double.parseDouble(a[20]), Double.parseDouble(a[21]),
                        Double.parseDouble(a[22]))).toList();
    }

    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> sum = courseList.stream()
                .collect(Collectors.groupingBy(Course::getInstitution,
                        Collectors.summingInt(Course::getParticipants)));
        Map<String, Integer> result = new LinkedHashMap<>();
        sum.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEachOrdered(o -> result.put(o.getKey(), o.getValue()));
        return result;
    }

    /*
    public Map<String,Integer> getPtcpCountByInstAndSubject(){
        Map<String,Integer> result = new HashMap<>();
        courseList.forEach(o->{
            o.getCourseSubjectDivide().forEach(l->{
                if(result.containsKey(o.getInstitution()+"-"+l)){
                    result.put(o.getInstitution()+"-"+l,
                    result.get(o.getInstitution()+"-"+l)+o.getParticipants());
                }
                else{
                    result.put(o.getInstitution()+"-"+l,o.getParticipants());
                }
            });
        });
        Map<String,Integer> ans = new LinkedHashMap<>();
        result.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEachOrdered(o->ans.put(o.getKey(),o.getValue()));
         return ans;
    }
    */

    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> result = new HashMap<>();
        courseList.forEach(o -> {
            if (result.containsKey(o.getInstitution() + "-" + o.getCourseSubjectPre())) {
                result.put(o.getInstitution() + "-" + o.getCourseSubjectPre(),
                        result.get(o.getInstitution() + "-"
                                + o.getCourseSubjectPre()) + o.getParticipants());
            } else {
                result.put(o.getInstitution() + "-" + o.getCourseSubjectPre(), o.getParticipants());
            }
        });
        Map<String, Integer> ans = new LinkedHashMap<>();
        result.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> ans.put(o.getKey(), o.getValue()));
        return ans;
    }

    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> result = new TreeMap<>();
        //Map<String, List<List<String>>> ans = new TreeMap<>();
        courseList.forEach(o -> {
            if (o.oneInstructor) {
                if (result.containsKey(o.getInstructorDivide().get(0))) {
                    List<String> one = result.get(o.getInstructorDivide().get(0)).get(0);
                    if (!one.contains(o.getCourseTitlePre()))  {
                        one.add(o.getCourseTitlePre());
                    }
                    List<List<String>> newOne = result.get(o.getInstructorDivide().get(0));
                    newOne.set(0, one);
                    result.put(o.getInstructorDivide().get(0), newOne);
                } else {
                    List<String> one = new ArrayList<>();
                    one.add(o.getCourseTitlePre());
                    List<List<String>> newOne = new ArrayList<>();
                    newOne.add(one);
                    newOne.add(new ArrayList<>());
                    result.put(o.getInstructorDivide().get(0), newOne);
                }
            } else {
                o.getInstructorDivide().forEach(l -> {
                    if (result.containsKey(l)) {
                        List<String> two = result.get(l).get(1);
                        if (!two.contains(o.getCourseTitlePre())) {
                            two.add(o.getCourseTitlePre());
                        }
                        List<List<String>> newOne = result.get(l);
                        newOne.set(1, two);
                        result.put(l, newOne);
                    } else {
                        List<String> two = new ArrayList<>();
                        two.add(o.getCourseTitlePre());
                        List<List<String>> newOne = new ArrayList<>();
                        newOne.add(new ArrayList<>());
                        newOne.add(two);
                        result.put(l, newOne);
                    }
                });
            }
        });
        result.forEach((key, value) -> {
            value.get(0).sort(String::compareTo);
            value.get(1).sort(String::compareTo);
        });
        return result;
        //ans = result.entrySet().stream().sorted(Map.Entry.comparingByKey())
    }

    public List<String> getCourses(int topK, String by) {
        //Map<String,String> relate = new HashMap<>();
        //courseList.forEach(o->relate.put(o.getCourseNumber(),o.courseTitle));
        //relate.forEach((o1,o2)-> System.out.println(o1+" "+o2));
        Map<String, Double> result = new HashMap<>();
        Map<String, Integer> result1 = new HashMap<>();
        List<String> ans = new ArrayList<>();
        if (by.equals("hours")) {
            courseList.stream().collect(Collectors
                            .groupingBy(Course::getCourseTitlePre,
                                    Collectors.maxBy(Comparator
                                            .comparingDouble(Course::getCourseHours))))
                    .forEach((o1, o2) -> result.put(o1, o2.get().getCourseHours()));
            /*
            result.keySet().forEach(System.out::println);
            result.entrySet().stream().
            sorted(Map.Entry.<String,Double>comparingByValue().reversed()).
            limit(topK).forEach(o->ans.add(relate.get(o.getKey())));
            */
            result.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(topK)
                    .forEach(o -> ans.add(o.getKey()));
        } else if (by.equals("participants")) {
            courseList.stream().collect(Collectors.groupingBy(Course::getCourseTitlePre,
                            Collectors.maxBy(Comparator.comparingInt(Course::getParticipants))))
                    .forEach((o1, o2) -> result1.put(o1, o2.get().getParticipants()));
            result1.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(topK)
                    .forEach(o -> ans.add(o.getKey()));
        }
        return ans;
    }

    /*
    public List<String> searchCourses
    (String courseSubject, double percentAudited, double totalCourseHours){
        List<String> ans = new ArrayList<>();
        courseList.stream().filter(o->o.getAuditedPercent()>=percentAudited).
        filter(o->o.getCourseHours()<=totalCourseHours)
                .forEach(o-> o.getCourseSubjectDivide().forEach(l->{
                    if(l.compareToIgnoreCase(courseSubject)==0){
                        ans.add(o.getCourseTitlePre());
                    }
                }));
        return ans.stream().distinct().sorted(String::compareTo).toList();
    }
     */

    public List<String> searchCourses(String courseSubject,
                                      double percentAudited, double totalCourseHours) {
        List<String> ans = new ArrayList<>();
        courseList.stream().filter(o -> o.getAuditedPercent() >= percentAudited)
                .filter(o -> o.getCourseHours() <= totalCourseHours)
                .forEach(o -> {
                    if (o.getCourseSubjectPre().toLowerCase()
                            .contains(courseSubject.toLowerCase())) {
                        ans.add(o.getCourseTitlePre());
                    }
                });
        return ans.stream().distinct().sorted(String::compareTo).toList();
    }

    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, Double> similarity = new LinkedHashMap<>();
        Map<String, Double> choose = new LinkedHashMap<>();   //title and similarity
        Map<String, List<Course>> courseSet = courseList.stream()
                .collect(Collectors.groupingBy(Course::getCourseNumber));
        Map<String, String> courseN = new LinkedHashMap<>();    //number and title
        List<String> ans = new ArrayList<>();
        List<String> result = new ArrayList<>();
        courseSet.forEach((o1, o2) -> {
            o2.sort((a, b) -> b.getAdjustDate().compareTo(a.getAdjustDate()));
            courseN.put(o1, o2.get(0).getCourseTitlePre());
        });

        Map<String, Double> medianAge = courseList.stream()
                .collect(Collectors.groupingBy(Course::getCourseNumber,
                        Collectors.averagingDouble(Course::getMedianAge)));
        Map<String, Double> medianGender = courseList.stream()
                .collect(Collectors.groupingBy(Course::getCourseNumber,
                        Collectors.averagingDouble(Course::getMalePercent)));
        Map<String, Double> medianDegree = courseList.stream()
                .collect(Collectors.groupingBy(Course::getCourseNumber,
                        Collectors.averagingDouble(Course::getDegreePercent)));
        medianAge.keySet().forEach(o ->
                similarity.put(o, Math.pow((age - medianAge.get(o)), 2)
                        + Math.pow((gender * 100 - medianGender.get(o)), 2)
                        + Math.pow(isBachelorOrHigher * 100 - medianDegree.get(o), 2)));
        /*
        similarity.entrySet().stream().sorted(Map.Entry.comparingByValue())
        .forEachOrdered(o->choose.add(o.getKey()));
        choose.forEach(o->ans.add(courseN.get(o)));
        ans.stream().distinct().limit(10).forEachOrdered(result::add);
        return result;
        */
        similarity.forEach((key, value) -> choose.put(courseN.get(key), value));
        choose.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .sorted(Map.Entry.comparingByValue()).forEachOrdered(o -> ans.add(o.getKey()));
        ans.stream().distinct().limit(10).forEachOrdered(result::add);
        return result;
    }


    public static class Course {
        final private String institution;
        final private String courseNumber;
        final private String launchDate;
        final private String adjustDate;
        final private String courseTitle;
        final private String courseTitlePre;
        final private String instructors;
        final private String instructorPre;
        final private List<String> instructorDivide;
        final private boolean oneInstructor;
        final private String courseSubject;
        final private String courseSubjectPre;
        final private List<String> courseSubjectDivide;
        final private int year;
        final private int honorCode;
        final private int participants;
        final private int audited;
        final private int certified;
        final private double auditedPercent;
        final private double certifiedPercent;
        final private double certifiedPercentHalf;
        final private double playedVideoPercent;
        final private double postedPercent;
        final private double higherThanZeroPercent;
        final private double courseHours;
        final private double medianHours;
        final private double medianAge;
        final private double malePercent;
        final private double femalePercent;
        final private double degreePercent;

        public Course(String institution, String courseNumber, String launchDate,
                      String courseTitle, String instructors, String courseSubject, int year,
                      int honorCode, int participants, int audited, int certified,
                      double auditedPercent, double certifiedPercent, double certifiedPercentHalf,
                      double playedVideoPercent, double postedPercent,
                      double higherThanZeroPercent, double courseHours, double medianHours,
                      double medianAge, double malePercent,
                      double femalePercent, double degreePercent) {
            this.institution = institution;
            this.courseNumber = courseNumber;
            this.launchDate = launchDate;
            String [] date = launchDate.split("/");
            this.adjustDate = date[2].concat(date[0]).concat(date[1]);
            this.courseTitle = courseTitle;
            this.instructors = instructors;
            this.courseTitlePre = courseTitle.replace("\"", "");
            this.instructorPre = instructors.replace("\"", "");
            this.instructorDivide = Arrays.stream(instructorPre.split(", ")).toList();
            this.oneInstructor = instructorDivide.size() == 1;
            this.courseSubject = courseSubject;
            this.courseSubjectPre = courseSubject.replace("\"", "");
            this.courseSubjectDivide = Arrays.stream(courseSubjectPre.split(", "))
                    .map(l -> l.replace("and ", "")).toList();
            this.year = year;
            this.honorCode = honorCode;
            this.participants = participants;
            this.audited = audited;
            this.certified = certified;
            this.auditedPercent = auditedPercent;
            this.certifiedPercent = certifiedPercent;
            this.certifiedPercentHalf = certifiedPercentHalf;
            this.playedVideoPercent = playedVideoPercent;
            this.postedPercent = postedPercent;
            this.higherThanZeroPercent = higherThanZeroPercent;
            this.courseHours = courseHours;
            this.medianHours = medianHours;
            this.medianAge = medianAge;
            this.malePercent = malePercent;
            this.femalePercent = femalePercent;
            this.degreePercent = degreePercent;
        }

        public String getInstitution() {
            return institution;
        }

        public String getInstructorPre() {
            return instructorPre;
        }

        public String getCourseNumber() {
            return courseNumber;
        }

        public String getLaunchDate() {
            return launchDate;
        }

        public String getAdjustDate() {
            return adjustDate;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public String getCourseTitlePre() {
            return courseTitlePre;
        }

        public List<String> getInstructorDivide() {
            return instructorDivide;
        }

        public List<String> getCourseSubjectDivide() {
            return courseSubjectDivide;
        }

        public String getInstructors() {
            return instructors;
        }

        public boolean isOneInstructor() {
            return oneInstructor;
        }

        public String getCourseSubjectPre() {
            return courseSubjectPre;
        }

        public String getCourseSubject() {
            return courseSubject;
        }

        public int getYear() {
            return year;
        }

        public int getHonorCode() {
            return honorCode;
        }

        public int getParticipants() {
            return participants;
        }

        public int getAudited() {
            return audited;
        }

        public int getCertified() {
            return certified;
        }

        public double getAuditedPercent() {
            return auditedPercent;
        }

        public double getCertifiedPercent() {
            return certifiedPercent;
        }

        public double getCertifiedPercentHalf() {
            return certifiedPercentHalf;
        }

        public double getPlayedVideoPercent() {
            return playedVideoPercent;
        }

        public double getPostedPercent() {
            return postedPercent;
        }

        public double getHigherThanZeroPercent() {
            return higherThanZeroPercent;
        }

        public double getCourseHours() {
            return courseHours;
        }

        public double getMedianHours() {
            return medianHours;
        }

        public double getMedianAge() {
            return medianAge;
        }

        public double getMalePercent() {
            return malePercent;
        }

        public double getFemalePercent() {
            return femalePercent;
        }

        public double getDegreePercent() {
            return degreePercent;
        }

    }
}
