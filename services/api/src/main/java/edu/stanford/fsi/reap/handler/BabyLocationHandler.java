package edu.stanford.fsi.reap.handler;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class BabyLocationHandler {

  private static final Logger log = LoggerFactory.getLogger(BabyLocationHandler.class);

  private static final String SPECIAL_AREA = "市辖区";

  private static final String LOCATION_SEARCH = "https://restapi.amap.com/v3/place/text";

  @Value("${application.map.key}")
  private String key;

  private final RestTemplate restTemplate = new RestTemplate();

  private Gson gson = new Gson();

  public String confirmBabyLocation(String area, String location) {
    String requestUrl = wrapperUrl(area, location);
    String result = restTemplate.getForObject(requestUrl, String.class);
    log.info("获取地址经纬度结果是：->{}->请求地址是：{},请求参数是：{}", result, requestUrl, area + "-" + location);
    String locationResult = null;
    if (StringUtils.isEmpty(result)) {
      log.warn("获取地址经纬度出现异常->{}->请求地址是：{}", result);
    } else {
      GdWebApiResponse gdWebApiResponse = gson.fromJson(result, GdWebApiResponse.class);
      if (!gdWebApiResponse.getInfo().equals("OK")) {
        log.warn("获取地址经纬度出现异常->{}->请求地址是：{},请求参数是：{}", result, requestUrl, area + "-" + location);
      } else {
        List<Poi> pois = gdWebApiResponse.getPois();
        if (!CollectionUtils.isEmpty(pois)) {
          Poi poi = pois.get(0);
          locationResult = poi.getLocation();
        }
      }
    }
    if (StrUtil.isEmpty(locationResult)) {
      requestUrl = wrapperUrl(area);
      result = restTemplate.getForObject(requestUrl, String.class);
      if (StringUtils.isEmpty(result)) {
        log.warn("获取地址经纬度出现异常->{}->请求地址是：{}", result);
      } else {
        GdWebApiResponse gdWebApiResponse = gson.fromJson(result, GdWebApiResponse.class);
        if (!gdWebApiResponse.getInfo().equals("OK")) {
          log.warn("获取地址经纬度出现异常->{}->请求地址是：{},请求参数是：{}", result, requestUrl, area + "-" + location);
        } else {
          List<Poi> pois = gdWebApiResponse.getPois();
          if (!CollectionUtils.isEmpty(pois)) {
            Poi poi = pois.get(0);
            locationResult = poi.getLocation();
          }
        }
      }
    }
    return locationResult;
  }

  private String wrapperUrl(String area, String location) {
    String[] areaSplits = area.split("/");
    if (areaSplits.length <= 1) {
      log.warn("当前要查询的地区是：{},位置是：{}", area, location);
      throw new BadRequestAlertException("宝宝地址需要精确到市");
    }
    String city = areaSplits[1];
    StringBuilder keywords = new StringBuilder();
    if (SPECIAL_AREA.equals(city)) {
      city = areaSplits[0];
      keywords.append(areaSplits[2]).append(areaSplits[3]).append(location);
    } else {
      for (int i = 1; i < areaSplits.length; i++) {
        keywords.append(areaSplits[i]);
      }
      keywords.append(location);
    }

    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder
        .append(LOCATION_SEARCH)
        .append("?key=")
        .append(key)
        .append("&keywords=")
        .append(keywords)
        .append("&types=120203&120300&120301&120302&120303&120304")
        .append("&city=")
        .append(city)
        .append("&citylimit=true")
        .append("&children=1")
        .append("&extensions=base");
    return urlBuilder.toString();
  }

  private String wrapperUrl(String area) {
    String[] areaSplits = area.split("/");
    if (areaSplits.length <= 1) {
      log.warn("当前要查询的地区是：{},位置是：{}", area);
      throw new BadRequestAlertException("宝宝地址需要精确到市");
    }
    String city = areaSplits[1];
    StringBuilder keywords = new StringBuilder();
    if (SPECIAL_AREA.equals(city)) {
      city = areaSplits[0];
      keywords.append(areaSplits[2]).append(areaSplits[3]);
    } else {
      for (int i = 1; i < areaSplits.length; i++) {
        keywords.append(areaSplits[i]);
      }
    }

    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder
        .append(LOCATION_SEARCH)
        .append("?key=")
        .append(key)
        .append("&keywords=")
        .append(keywords)
        .append("&types=120203&120300&120301&120302&120303&120304")
        .append("&city=")
        .append(city)
        .append("&citylimit=true")
        .append("&children=1")
        .append("&extensions=base");
    return urlBuilder.toString();
  }

  @Data
  public static class GdWebApiResponse {

    private int status;

    private String info;

    private int infocode;

    private int count;

    private List<Poi> pois;
  }

  @Data
  public static class Poi {
    private String name;
    private String address;
    private String location;
  }
}
