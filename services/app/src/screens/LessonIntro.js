import React, { useEffect, useRef } from "react";
import { LinearGradient } from "expo-linear-gradient";

import Visits from "../utils/visit";
import storage from "../cache/storage";
import Http from "../utils/http";
import { Colors } from "../constants";
import { styled } from "../utils/styled";
import {
  GhostNavigatorHeader,
  BottomRightBackground,
  Button,
} from "../components";
import * as Location from "expo-location";
import { ToastAndroid } from "react-native";
import { useTranslation } from "react-i18next";

export default function LessonIntro({ navigation, route }) {
  const [lesson] = storage.useLesson(route.params?.id);
  const initialization = useRef(Promise.resolve(true));

  const { t } = useTranslation();
  useEffect(() => {
    if (!route.params.preview) {
      initialization.current = Http.session()
        .then(async (session) => {
          await storage.setUncommittedVisitStatus(
            route.params?.visitId,
            "UNDONE",
            Visits.formatDateTime(new Date()),
            session.account,
          );
          const cached =
            Number(await storage.getNextModule(route.params.visitId)) || 0;
          const resumed = route.params.continue
            ? Number(route.params.nextModuleIndex) || 0
            : 0;
          await storage.setNextModule(
            Math.max(cached, resumed),
            route.params.visitId,
            session.account,
          );
          return true;
        })
        .catch(() => {
          ToastAndroid.show(
            t("SignIn:offlineAccountRecovery"),
            ToastAndroid.LONG,
          );
          return false;
        });
    } else {
      initialization.current = Promise.resolve(true);
    }
  }, [route.params?.visitId, route.params?.preview]);

  return (
    <Container {...Colors.linearGradient}>
      <BottomRightBackground
        width={280}
        height={268}
        source={require("../assets/images/curriculum-bg.png")}
      />
      <GhostNavigatorHeader navigation={navigation} />
      <TextContainer>
        <Name>{lesson.name}：</Name>
        <Description>{lesson.description}</Description>
      </TextContainer>
      <ButtonContainer>
        <Button
          type="info"
          title={t("Session:next")}
          onPress={async () => {
            if (!(await initialization.current)) return;
            Location.requestForegroundPermissionsAsync().then(
              async ({ status }) => {
                if (status !== "granted") {
                  ToastAndroid.show(
                    t("Session:locationPermissionMessage"),
                    ToastAndroid.LONG,
                  );
                  navigation.goBack();
                  return;
                }
                const hasEnableLocation =
                  await Location.hasServicesEnabledAsync();
                if (!hasEnableLocation) {
                  ToastAndroid.show(
                    t("Session:locationClosedMessage"),
                    ToastAndroid.LONG,
                  );
                  return;
                }
                navigation.navigate("LessonModules", route.params);
              },
            );
          }}
        />
      </ButtonContainer>
    </Container>
  );
}

const TextContainer = styled.View`
  padding: 0 28px;
`;

const Container = styled(LinearGradient)`
  width: 100%;
  height: 100%;
  position: relative;
`;

const Name = styled.Text`
  width: 240px;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  margin-top: 24px;
`;

const Description = styled.Text`
  color: #fff;
  font-size: 10px;
  margin-top: 24px;
`;

const ButtonContainer = styled.View`
  position: absolute;
  bottom: 60px;
  width: 100%;
`;
