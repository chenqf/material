package com.maple;

import static org.junit.jupiter.api.Assertions.assertTrue;


import com.maple.mongo.MongoApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit test for simple App.
 */
@SpringBootTest(classes = MongoApplication.class)
@RunWith(SpringRunner.class)
public class AppTest 
{
    @Test
    public void shouldAnswerWithTrue()
    {

        System.out.println("11111111111");
        assertTrue( true );
    }
}
