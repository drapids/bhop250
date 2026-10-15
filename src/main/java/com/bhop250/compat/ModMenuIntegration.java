package com.bhop250.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import com.bhop250.Bhop250Client;
import com.bhop250.config.Bhop250ConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (!Bhop250Client.isClothConfigLoaded()) {
			return ModMenuApi.super.getModConfigScreenFactory();
		}
		return parent -> Bhop250ConfigScreen.create(parent);
	}
}
