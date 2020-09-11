<template>
  <div>
    <Field label="Nombre" label-position="on-border" hideErrorMessage :v="validation.allErrors('user.name')">
      <b-input v-model="user.name" ref="txtNombre" placeholder="Nombre" type="text" :disabled="disabled"> </b-input>
    </Field>
    <Field label="E-Mail" label-position="on-border" hideErrorMessage :v="validation.allErrors('user.email')">
      <b-input v-model="user.email" placeholder="E-Mail" type="text" :disabled="disabled"> </b-input>
    </Field>
    <Field label="Usuario" label-position="on-border" hideErrorMessage :v="validation.allErrors('user.username')">
      <b-input v-model="user.username" placeholder="Usuario" type="text" icon="account" :disabled="disabled"> </b-input>
    </Field>
    <Field label="Password" label-position="on-border" :v="validation.allErrors('user.password')">
      <b-input
        v-model="user.password"
        placeholder="Contraseña"
        type="password"
        icon="pound"
        password-reveal
        :disabled="disabled"
      />
    </Field>
    <Field label="Confirmar Password" label-position="on-border" :v="validation.allErrors('passwordConfirm')">
      <b-input
        v-model="passwordConfirm"
        placeholder="Confirmar"
        type="password"
        icon="pound"
        password-reveal
        :disabled="disabled"
      />
    </Field>
    <b-field>
      <b-button
        type="is-info"
        icon-left="login"
        :loading="loading"
        :disabled="disabled"
        expanded
        @click="onRegisterClick"
      >
        Guardar
      </b-button>
    </b-field>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Focusable } from '@/utils/browser';
import { User, UserWithPassword } from '../../model/User';
import { required, field } from '../../utils/validation';
import SimpleVueValidation from 'simple-vue-validator';
import { isNullOrUndefined } from '../../utils/general';
const Validator = SimpleVueValidation.Validator;

@Component({
  validators: {
    'user.username': required,
    'user.name': required,
    'user.email': function(value) {
      return field(value)
        .required()
        .email();
    },
    'user.password': function(this: UserForm, value: string) {
      let v = Validator.value(value ?? null);
      if (this.requirePassword) {
        v = v.required().minLength(4);
      }
      return v;
    },
    'passwordConfirm, user.password': function(this: UserForm, repeat, password) {
      if (this.validation.isTouched('passwordConfirm') || this.validation.isTouched('user.password')) {
        return Validator.value(repeat ?? null)
          .required()
          .match(password);
      } else {
        return Validator.value(repeat ?? null);
      }
    }
  }
})
export default class UserForm extends Vue {
  $refs: { txtNombre: Vue & Focusable };

  @Prop()
  value: User;

  @Prop({ type: Boolean })
  loading: boolean;

  @Prop({ type: Boolean })
  disabled: boolean;

  @Prop({ type: Boolean })
  requirePassword: boolean;

  user: Partial<UserWithPassword> = {};
  passwordConfirm = '';

  @Watch('value')
  onValueChange() {
    this.passwordConfirm = '';
    if (isNullOrUndefined(this.value)) {
      this.user = {};
    } else {
      this.user = JSON.parse(JSON.stringify(this.value));
    }
    this.validation.reset();
  }

  mounted() {
    this.$refs.txtNombre.focus();
    this.onValueChange();
  }

  // onKeyDown(e: KeyboardEvent) {
  //   if (e.keyCode === 13) {
  //     this.onLoginClick();
  //   }
  // }

  validate() {
    return this.$validate();
  }

  async onRegisterClick() {
    if (await this.validate()) {
      this.$emit('save', this.user);
    }
  }
}
</script>
