package edu.stanford.fsi.reap.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

public class DistanceUtils {
  /**
   * 根据经纬度计算距离
   *
   * @param lon1 当前经度
   * @param lat1 当前纬度
   * @param lon2 目的经度
   * @param lat2 目的纬度
   * @return 距离 （km）
   */
  public static double getDistance(Double lon1, Double lat1, Double lon2, Double lat2) {
    GlobalCoordinates source = new GlobalCoordinates(lat1, lon1);
    GlobalCoordinates target = new GlobalCoordinates(lat2, lon2);
    GeodeticCurve geoCurve =
        new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, source, target);
    double distance = geoCurve.getEllipsoidalDistance();
    BigDecimal distanceBig = new BigDecimal(distance).setScale(2, RoundingMode.UP);
    distanceBig = distanceBig.multiply(new BigDecimal("0.001")).setScale(2, RoundingMode.UP);
    return distanceBig.doubleValue();
  }
}
