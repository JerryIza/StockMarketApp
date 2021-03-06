package com.example.apistock.ui.viewmodels;

import com.example.apistock.data.api.MainRepository;
import com.example.apistock.utils.MyPreference;
import dagger.internal.Factory;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class LoginViewModel_AssistedFactory_Factory implements Factory<LoginViewModel_AssistedFactory> {
  private final Provider<MainRepository> repositoryProvider;

  private final Provider<MyPreference> myPreferenceProvider;

  public LoginViewModel_AssistedFactory_Factory(Provider<MainRepository> repositoryProvider,
      Provider<MyPreference> myPreferenceProvider) {
    this.repositoryProvider = repositoryProvider;
    this.myPreferenceProvider = myPreferenceProvider;
  }

  @Override
  public LoginViewModel_AssistedFactory get() {
    return newInstance(repositoryProvider, myPreferenceProvider);
  }

  public static LoginViewModel_AssistedFactory_Factory create(
      Provider<MainRepository> repositoryProvider, Provider<MyPreference> myPreferenceProvider) {
    return new LoginViewModel_AssistedFactory_Factory(repositoryProvider, myPreferenceProvider);
  }

  public static LoginViewModel_AssistedFactory newInstance(Provider<MainRepository> repository,
      Provider<MyPreference> myPreference) {
    return new LoginViewModel_AssistedFactory(repository, myPreference);
  }
}
