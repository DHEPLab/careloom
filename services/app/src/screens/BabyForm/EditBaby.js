import React from "react";

import http from "../../utils/http";
import BabyForm from "./BabyForm";
import { styled } from "@/utils/styled";
import confirm from "../../components/modal/confirm";
import { useDispatch } from "react-redux";
import i18next from "i18next";

export default function EditBaby({ navigation, route }) {
  const { params } = route;
  const dispatch = useDispatch();
  function onSubmit(values) {
    if (!params.id) return;
    confirm(i18next.t("CreateCarer:confirmEdit"), {
      onOk: async () => {
        await http.put(`/api/babies/${params.id}`, values);
        navigation.navigate(params.from, {
          success: Math.random(),
          ...params,
          ...(params.prevParams || {}),
        });
      },
      dispatch,
    });
  }

  return (
    <Container>
      <BabyForm onSubmit={onSubmit} initialValues={params.baby} />
    </Container>
  );
}

const Container = styled.View`
  padding: 20px 28px;
`;
