<template>
  <div>
    <!-- Username -->
    <Field label="Usuario" label-position="on-border" hideErrorMessage :v="validation.allErrors('username')">
      <b-input
        v-model="username"
        ref="txtUsuario"
        placeholder="Usuario"
        type="text"
        icon="account"
        :disabled="loading"
        @keydown.native="onKeyDown"
      >
      </b-input>
    </Field>

    <!-- Password -->
    <Field label="Contraseña" label-position="on-border" hideErrorMessage :v="validation.allErrors('password')">
      <b-input
        v-model="password"
        placeholder="Contraseña"
        type="password"
        icon="pound"
        password-reveal
        :disabled="loading"
        @keydown.native="onKeyDown"
      />
    </Field>
    <!-- <b-field>
      <b-checkbox>
        Recordarme?
      </b-checkbox>
    </b-field> -->

    <!-- Boton -->
    <b-field>
      <b-button type="is-info" icon-left="login" expanded :loading="loading" @click="onLoginClick">
        Ingresar
      </b-button>
    </b-field>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Focusable } from '@/utils/browser';
import { required } from '../../utils/validation';

/** Form de Autenticacion */
@Component({
  validators: {
    username: required,
    password: required
  }
})
export default class LoginForm extends Vue {
  @Prop({ type: Boolean })
  loading: boolean;

  /** Usuario ingresado en el form */
  private username = '';

  /** Password ingresado en el form */
  private password = '';

  $refs: { txtUsuario: Vue & Focusable };

  private mounted() {
    this.$refs.txtUsuario.focus();
  }

  /**
   * Handler cuando se presiona una tecla en cualquier input.
   * Se usa para usar el ENTER como sinonimo de hacer click en el boton
   */
  private onKeyDown(e: KeyboardEvent) {
    if (e.keyCode === 13) {
      this.onLoginClick();
    }
  }

  /** Handler cuando se hace click en el boton de login */
  private async onLoginClick() {
    // Se valida el form
    if (await this.$validate()) {
      // Se lanza el evento
      this.$emit('submit', { username: this.username, password: this.password });

      // Se limpia el password
      this.password = '';
    }
  }
}
</script>
