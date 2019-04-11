package capture.util;

import capture.entity.ChangeInfo;
import capture.mapper.ChangeInfoMapper;
import com.oscroll.strawboat.assets.entity.IP;
import com.oscroll.strawboat.pool.ScheduledPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 根据企业名称获取企业的变更记录.
 * 通过企查查的网站提供的信息获取
 */
public class CaptureUtil {
    MapperUtil mapperUtil = new MapperUtil();
    //向数据库中保存所有客户公司的变更记录
    private ChangeInfoMapper changeInfoMapper = SpringContextHolder.getBean(ChangeInfoMapper.class);

    //企查查网址主页
    private String qichachaUrl = "https://www.qichacha.com/";
    //企查查企业搜索url
    private String searchUrl = "https://www.qichacha.com/search?key=";

    /**
     * 取得当前所有的企业名称
     *
     * @return
     */
    public List<String> allCompanyNames() {
        return changeInfoMapper.getAllCompanyName();
    }

    /**
     * 获取一个企业的变更记录
     *
     * @param companyName 企业在企查查网站中的地址
     * @return 返回企业的所有的变更记录信息, 如果没有变更记录返回null
     * @throws Exception
     */
    public List<ChangeInfo> getChangeInfo(String companyName) throws Exception {
        String url = this.getCompanyUrl(companyName);
        if (url == null || "".equals(url)) {
            return null;
        }
        //模拟浏览器访问网页,得到网页的Document对象
        Document document = getHtmlDoc(url);
        // 保存最后得到的数据
        List<ChangeInfo> allInfos = new ArrayList<>();
        //获取变更记录信息
        Element changeList = document.getElementById("Changelist");
        if (changeList == null || "".equals(changeList)) {
            return null;
        }

        Element table = changeList.getElementsByTag("table").get(0);
        Elements trs = table.getElementsByTag("tr");
        //跳过第一行表头:序号 变更日期 变更项目 变更前 变更后
        for (int x = 1; x < trs.size(); x++) {
            Elements tds = trs.get(x).getElementsByTag("td");
            ChangeInfo row = new ChangeInfo();
            row.setCompanyName(companyName);
            row.setChangeDate(stringToDate(tds.get(1).text()));
            row.setProjectName(tds.get(2).text());
            row.setBeforeInfo(tds.get(3).text());
            row.setAfterInfo(tds.get(4).text());
            row.setId(UUIDUtil.uuid());
            allInfos.add(row);
        }
        return allInfos;
    }

    /**
     * 得到所有客户公司的变更信息
     * <p>
     * 使用Map集合返回,key为公司名称,value=list集合,
     * list集合保存了该公司的每一条变更记录.
     *
     * @return
     * @throws Exception
     */
    public Map<String, List<ChangeInfo>> getChangeInfos() throws Exception {
        List<String> copanyNames = this.changeInfoMapper.getAllCompanyName();
        Map<String, List<ChangeInfo>> allChangeInfo = new HashMap<>();
        for (String copanyName : copanyNames) {
            List<ChangeInfo> changeInfo = this.getChangeInfo(copanyName);
            allChangeInfo.put(copanyName, changeInfo);
        }
        return allChangeInfo;
    }

    /**
     * 更新数据库中 cpms_change_info表中的记录
     * 根据就数据库中的记录数判断客户企业是否有新的变更记录
     */
    public void updateChangeInfoData() {
        System.out.println("********开始更新企业变更记录********");
        Map<String, List<ChangeInfo>> allChangeInfos;
        //获取最新的企业变更信息
        try {
            allChangeInfos = this.getChangeInfos();

            //如果是第一次调用
            if (this.changeInfoMapper.getAllCount() == 0) {
                for (Map.Entry<String, List<ChangeInfo>> temp : allChangeInfos.entrySet()) {
                    if (temp.getValue() != null) {
                        this.changeInfoMapper.insertByBatch(temp.getValue());
                    }
                }
            } else {
                //根据总记录数判断是否有新的变更信息
                int oldCount = this.changeInfoMapper.getAllCount();
                Integer newCount = 0;//保存新的记录数
                for (Map.Entry<String, List<ChangeInfo>> temp : allChangeInfos.entrySet()) {
                    List<ChangeInfo> infos = temp.getValue();
                    if (infos != null) {
                        newCount += infos.size();
                    }
                }
                //说明有新的变更记录
                if (oldCount < newCount) {
                    //判断哪一个企业有变更记录
                    for (Map.Entry<String, List<ChangeInfo>> temp : allChangeInfos.entrySet()) {
                        List<ChangeInfo> companyChangeInfo = temp.getValue();
                        Integer oldCompanyCount = this.changeInfoMapper.getCountByCompany(temp.getKey());
                        Integer newCompanyCount = 0;
                        if (companyChangeInfo != null) {
                            newCompanyCount = temp.getValue().size();
                        }
                        if (oldCompanyCount < newCompanyCount) {
                            //删除原有的旧记录
                            this.changeInfoMapper.deleteByCompany(temp.getKey());
                            //添加新的记录
                            this.changeInfoMapper.insertByBatch(temp.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新一个企业的变更记录
     *
     * @return
     */
    public void updateChangeInfoByName(String companyName) {
        try {
            //获取该企业最新的变更记录
            List<ChangeInfo> newInfo = this.getChangeInfo(companyName);
            //获取数据库中当前的该企业变更记录总量
            Integer count = this.changeInfoMapper.getCountByCompany(companyName);
            //只有当有新的变更记录时,才会更新数据库中的数据
            if (newInfo != null && newInfo.size() > count) {
                if (count > 0) {
                    // 清空旧记录
                    this.changeInfoMapper.deleteByCompany(companyName);
                }
                //添加新纪录
                this.changeInfoMapper.insertByBatch(newInfo);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 获取企查查中 每个公司名称在企查查网站中对应的访问路径
     * 例如 福州铁越建筑咨询有限公司
     * /firm_80a2c2edf4d689b05f1618c6eca3747c.html
     * <p>
     * 最后要拼接为 企查查主页+公司访问路径:
     * https//:www.qichacha.com/firm_80a2c2edf4d689b05f1618c6eca3747c.html
     *
     * @param companyName
     * @return
     */
    public String getCompanyUrl(String companyName) throws Exception {
        companyName = URLEncoder.encode(companyName, "UTF-8");
        String companyUrl = "";
        String newUrl = searchUrl + companyName;
        Document document = getHtmlDoc(newUrl);

        try {
            Element searchTbody = document.getElementById("search-result");
            //如果没有得到搜索结果,判断是否为重定向
            if (searchTbody == null) {
                Elements allScript = document.getElementsByTag("script");
                for (Element script : allScript) {
                    String text = script.toString();
                    //如果js代码中带有 window.location.href='' 表示重定向
                    if (text.indexOf("window.location.href") != -1) {
                        // 拆分处重定向路径
                        String[] redirect = text.split("'");
                        document = getHtmlDoc(text.split("'")[1]);
                        searchTbody = document.getElementById("search-result");
                        break;
                    }
                }
            }
            Elements allLink = searchTbody.getElementsByTag("a");
            for (Element a : allLink) {
                //将超链接中的中文转换为请求 key 参数一样的编码格式
                String linkInfo = URLEncoder.encode(a.text(), "UTF-8");
                //String linkInfo = a.text();
                if (linkInfo.equals(companyName)) {
                    companyUrl = a.attr("href");
                    break;
                }
            }
        } catch (Exception e) {
            return "";
        }
        return qichachaUrl + companyUrl;
    }

    /**
     * 根据一个url 将网页内容解析为一个Document 对象
     *
     * @param url 目标路径
     * @return 如果该路径不存在返回null
     * @throws Exception
     */
    private Document getHtmlDoc(String url) throws Exception {

        //模拟浏览器访问网页
        HttpURLConnection conn = null;
        URL realUrl = new URL(url);
        conn = (HttpURLConnection) realUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setReadTimeout(8000);
        conn.setConnectTimeout(8000);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.75 Safari/537.36");
        conn.setRequestProperty("Cookie","QCCSESSID=bfm2v42kn86i09apnsfjkookf0; UM_distinctid=16a0a135fd6112-053abb289d71fd-7a1b34-144000-16a0a135fd773a; CNZZDATA1254842228=1494821738-1554946409-https%253A%252F%252Fwww.baidu.com%252F%7C1554946409; zg_did=%7B%22did%22%3A%20%2216a0a13609b86c-03423b6f85a15b-7a1b34-144000-16a0a13609cbff%22%7D; Hm_lvt_3456bee468c83cc63fb5147f119f1075=1554947203; hasShow=1; _uab_collina=155494720354286595760425; acw_tc=6f48649a15549471980813809e6aa04ff199160e3558d4c406e403f047; Hm_lpvt_3456bee468c83cc63fb5147f119f1075=1554947393; zg_de1d1a35bfa24ce29bbf2c7eb17e6c4f=%7B%22sid%22%3A%201554947203230%2C%22updated%22%3A%201554947396067%2C%22info%22%3A%201554947203232%2C%22superProperty%22%3A%20%22%7B%7D%22%2C%22platform%22%3A%20%22%7B%7D%22%2C%22utm%22%3A%20%22%7B%7D%22%2C%22referrerDomain%22%3A%20%22www.baidu.com%22%7D");

        int code = conn.getResponseCode();
        if (code == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            String result = buffer.toString();
            //将得到的所有的数据解析为一个 Document对象
            Document document = Jsoup.parse(result);

            //断开连接
            in.close();
            conn.disconnect();
            return document;


        } else if (code == 301 || code == 302) {
            //当ip被限制查询时,重新链接宽带,更换ip地址
            // this.rebotadsl();
        }
        //断开连接
        conn.disconnect();
        return null;
    }

    /**
     * 将字符串转换为日期,不带时间
     *
     * @param str
     * @return
     */
    public Date stringToDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取随机的浏览器标识符
     *
     * @return
     */
    public String getRandomUserAgent() {

        String[] ua = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
                "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
                "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
                "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",
                "Mozilla/5.0 (Windows NT 6.1; W…) Gecko/20100101 Firefox/60.0",
                "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0",
                "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)",
                " Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon 2.0)",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; TencentTraveler 4.0)",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)",};
        int random = new Random().nextInt(ua.length);
        return ua[random];
    }


    /**
     * 获取一个随机可用的代理ip
     *
     * @return
     */
    public IP getIp() {
        //创建默认的IP池
        ScheduledPool pool = new ScheduledPool();
        //在线程中启动IP池
        new Thread(pool::execute).start();
        return pool.take();
    }


}