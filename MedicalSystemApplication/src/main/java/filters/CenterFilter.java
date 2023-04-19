package filters;

import dto.CenterFilterDTO;
import model.Center;

public class CenterFilter implements Filter {


    @Override
    public Boolean test(Object o1, Object o2) {
        try {
            Center c = (Center) o1;
            CenterFilterDTO d = (CenterFilterDTO) o2;

            Boolean flag = true;

            if (d.getName() != null) {
                if (!d.getName().equals("")) {
                    if (!c.getName().toLowerCase().contains(d.getName().toLowerCase())) {
                        flag = false;
                    }
                }
            }

            if (d.getAddress() != null) {
                if (!d.getAddress().equals("")) {
                    if (!c.getAddress().toLowerCase().contains(d.getAddress().toLowerCase())) {
                        flag = false;
                    }
                }
            }

            if (d.getState() != null) {
                if (!d.getState().equals("")) {
                    if (!c.getState().toLowerCase().contains(d.getState().toLowerCase())) {
                        flag = false;
                    }
                }
            }

            if (d.getCity() != null) {
                if (!d.getCity().equals("")) {
                    if (!c.getCity().toLowerCase().contains(d.getCity().toLowerCase())) {
                        flag = false;
                    }
                }
            }

            if (d.getRating() != 0) {
                if (Math.abs(c.calculateRating() - d.getRating()) > 1f) {
                    flag = false;
                }
            }

            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
