package capture.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import java.util.*;

public class TimedTask implements ServletContextListener {

    private CaptureUtil captureUtil = new CaptureUtil();
    //进行企业变更操作对象
    private List<String> companyNames = captureUtil.allCompanyNames();//保存当前数据库中所有企业名称
    private Random randTIme = new Random();
    //随机5分钟之内的时间,最小为1分钟
    private Integer intervalTime = randTIme.nextInt(3 * 60 * 1000 - 60 * 1000) + 60 * 1000;
    private Integer foot = companyNames.size() - 1;//操作集合的角标

    private final static Logger logger = Logger.getLogger(TimedTask.class);

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 指定的任务，从指定的延迟后，开始进行重复执行。
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

		   /*定制每天的0:00:00执行，若程序已超过0点启动,当天不再执行，等到明日0点再执行
		   这样保证了时间一直是8点，而不会变成程序启动时间*/

        calendar.set(year, month, day, 12, 0, 0);
        Date defaultdate = calendar.getTime();
        Date sendDate;
        // 8点后开机
        if (defaultdate.before(new Date())) {
            // 将发送时间设为明天8点
            calendar.add(Calendar.DATE, 1);
            sendDate = calendar.getTime();
        } else {
            //若此时时间没过8点，等待
            sendDate = defaultdate;
        }



        /*
         * ----------------每日任务 ----------------
         *
         */


        Timer dTimer = new Timer();

        dTimer.schedule(new TimerTask() {
            //初始化数据

            @Override
            public void run() {
                logger.debug("开始获取爬取企业变更记录" + companyNames.get(foot));
                //获取5分钟以内的随机时间,最小不低于1分钟
                intervalTime = randTIme.nextInt(3 * 60 * 1000 - 60 * 1000) + 60 * 1000;
                captureUtil.updateChangeInfoByName(companyNames.get(foot--));

                //当查询完所有的企业变更记录之后,更新数据库中所有企业名称,并设定12小时之后再执行
                if (foot == -1 || captureUtil == null) {
                    intervalTime = 12 * 60 * 60 * 1000;
                    captureUtil = new CaptureUtil();
                    companyNames = captureUtil.allCompanyNames();
                    foot = companyNames.size() - 1;
                }
            }
        }, sendDate, intervalTime);//每隔一段时间查询一次,最小不能够低于1分钟
    }
}
