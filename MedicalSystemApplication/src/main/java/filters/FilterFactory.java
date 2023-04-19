package filters;

public class FilterFactory {

    private static FilterFactory instance;

    public Filter get(String filterType) {
        if (filterType == null) {
            return null;
        }

        if (filterType.equalsIgnoreCase("Center")) {
            return new CenterFilter();
        }

        if (filterType.equalsIgnoreCase("HALL")) {
            return new HallFilter();
        }

        if (filterType.equalsIgnoreCase("PATIENT")) {
            return new PatientFilter();
        }

        if (filterType.equalsIgnoreCase("DOCTOR")) {
            return new DoctorFilter();
        }

        return null;
    }

    public static FilterFactory getInstance() {
        if (instance == null) {
            instance = new FilterFactory();
        }

        return instance;
    }
}
