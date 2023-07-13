import com.wp.common.DbTemplate;
import dao.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import java.util.List;
@Slf4j
public class test {

    @Test
    public void testSave() {
        User user = new User();
        user.setId(222);
        user.setName("翁");
        user.setPassword("12345");
        user.setPhoneNumber("1234589");
        user.setIsDelete(0);
        System.out.println(user);
        DbTemplate dbTemplate = new DbTemplate();
        try {
            dbTemplate.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testUpdate() {
        User user = new User();
        user.setId(222);
        user.setName("张");
        DbTemplate dbTemplate = new DbTemplate();
        try {
            dbTemplate.update(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDelete() {
        User user = new User();
        user.setId(222);
        DbTemplate dbTemplate = new DbTemplate();
        try {
            dbTemplate.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectAll() {
        User user = new User();
        DbTemplate dbTemplate = new DbTemplate();
        try {
            List<? extends User> users = dbTemplate.selectAll(user.getClass());
            for (User user1 : users) {
                log.info(user1.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectById() {
        User user = new User();
        DbTemplate dbTemplate = new DbTemplate();
        try {
            User user1 = dbTemplate.selectById(user.getClass(), 222);
            log.info(user1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
