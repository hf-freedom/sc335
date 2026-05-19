package com.housekeeping.config;

import com.housekeeping.entity.Aunt;
import com.housekeeping.entity.User;
import com.housekeeping.service.AuntService;
import com.housekeeping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private AuntService auntService;

    @Override
    public void run(String... args) {
        User user1 = new User();
        user1.setName("张三");
        user1.setPhone("13800138001");
        user1.setAddress("北京市朝阳区建国路88号");
        user1.setLatitude(new BigDecimal("39.9042"));
        user1.setLongitude(new BigDecimal("116.4074"));
        userService.createUser(user1);

        User user2 = new User();
        user2.setName("李四");
        user2.setPhone("13800138002");
        user2.setAddress("北京市海淀区中关村大街1号");
        user2.setLatitude(new BigDecimal("39.9842"));
        user2.setLongitude(new BigDecimal("116.3074"));
        userService.createUser(user2);

        User user3 = new User();
        user3.setName("王五");
        user3.setPhone("13800138003");
        user3.setAddress("北京市西城区金融街1号");
        user3.setLatitude(new BigDecimal("39.9142"));
        user3.setLongitude(new BigDecimal("116.3574"));
        userService.createUser(user3);

        Aunt aunt1 = new Aunt();
        aunt1.setName("李阿姨");
        aunt1.setPhone("13900139001");
        aunt1.setAddress("北京市朝阳区望京");
        aunt1.setLatitude(new BigDecimal("39.9942"));
        aunt1.setLongitude(new BigDecimal("116.4774"));
        aunt1.setSkillTags(Arrays.asList("日常保洁", "深度保洁", "做饭"));
        aunt1.setRating(new BigDecimal("4.8"));
        aunt1.setMaxDailyOrders(3);
        auntService.createAunt(aunt1);

        Aunt aunt2 = new Aunt();
        aunt2.setName("王阿姨");
        aunt2.setPhone("13900139002");
        aunt2.setAddress("北京市海淀区五道口");
        aunt2.setLatitude(new BigDecimal("39.9942"));
        aunt2.setLongitude(new BigDecimal("116.3374"));
        aunt2.setSkillTags(Arrays.asList("日常保洁", "月嫂育儿", "老人陪护"));
        aunt2.setRating(new BigDecimal("4.9"));
        aunt2.setMaxDailyOrders(2);
        auntService.createAunt(aunt2);

        Aunt aunt3 = new Aunt();
        aunt3.setName("张阿姨");
        aunt3.setPhone("13900139003");
        aunt3.setAddress("北京市丰台区");
        aunt3.setLatitude(new BigDecimal("39.8542"));
        aunt3.setLongitude(new BigDecimal("116.2874"));
        aunt3.setSkillTags(Arrays.asList("日常保洁", "深度保洁", "家电维修"));
        aunt3.setRating(new BigDecimal("4.5"));
        aunt3.setMaxDailyOrders(4);
        auntService.createAunt(aunt3);

        Aunt aunt4 = new Aunt();
        aunt4.setName("刘阿姨");
        aunt4.setPhone("13900139004");
        aunt4.setAddress("北京市东城区");
        aunt4.setLatitude(new BigDecimal("39.9242"));
        aunt4.setLongitude(new BigDecimal("116.4174"));
        aunt4.setSkillTags(Arrays.asList("做饭", "老人陪护", "月嫂育儿"));
        aunt4.setRating(new BigDecimal("4.7"));
        aunt4.setMaxDailyOrders(3);
        auntService.createAunt(aunt4);
    }
}
